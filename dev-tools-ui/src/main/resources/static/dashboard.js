(() => {
  const BASE_UI = `${window.location.origin}/dev-tools`;
  const BASE_API = `${window.location.origin}/exception-insights`;

  const SAMPLE = [
    {
      id: 'a1b2c3', timestamp: '2026-04-20T10:30:00Z', type: 'HTTP_REQUEST',
      exceptionClass: 'java.lang.NullPointerException', message: 'Cannot invoke getUser()',
      rootCauseClass: 'java.lang.NullPointerException', rootCauseMessage: 'Cannot invoke getUser()',
      httpMethod: 'POST', requestUri: '/api/users', requestBody: '{"name":"John"}',
      context: { thread: 'http-nio-8080', method: 'UserService.createUser' },
      recentLogs: [
        { level: 'WARN', message: 'Validation skipped', loggerName: 'com.example.UserService', threadName: 'http-nio-8080', timestamp: '2026-04-20T10:29:59Z' },
        { level: 'INFO', message: 'Request received: POST /api/users', loggerName: 'com.example.RequestFilter', threadName: 'http-nio-8080', timestamp: '2026-04-20T10:29:58Z' }
      ],
      aiExplanation: {
        summary: 'A null value was returned by getUser() and dereferenced without a null check, causing a NullPointerException during user creation.',
        causes: ['UserRepository.findById() returned null and was not checked before use', 'The user lookup was skipped due to missing validation in the request pipeline'],
        fixes: ['Add a null check after calling getUser() and return a 404 if the user is absent', 'Ensure request validation runs before the service layer is invoked', 'Consider using Optional<User> to make null-safety explicit']
      }
    },
    {
      id: 'd4e5f6', timestamp: '2026-04-20T10:28:00Z', type: 'SCHEDULED',
      exceptionClass: 'org.springframework.dao.DataAccessException', message: 'Unable to acquire JDBC connection',
      rootCauseClass: 'java.sql.SQLTransientConnectionException', rootCauseMessage: 'Connection pool exhausted',
      context: { thread: 'scheduling-1', method: 'ReportJob.generateDaily' },
      recentLogs: [
        { level: 'ERROR', message: 'Failed to obtain connection from pool', loggerName: 'com.zaxxer.hikari.pool.HikariPool', threadName: 'scheduling-1', timestamp: '2026-04-20T10:27:58Z' }
      ],
      aiExplanation: null
    },
  ];

  // ── State ─────────────────────────────────────────────────
  let errors     = [];
  let selectedId = null;
  let eventSource = null;
  let demoMode   = false;

  // ── Helpers ───────────────────────────────────────────────
  function shortClass(cls) { return cls ? cls.split('.').pop() : ''; }

  function fmtTime(ts) {
    if (!ts) return '';
    return new Date(ts).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  function fmtDateTime(ts) {
    if (!ts) return '';
    return new Date(ts).toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' });
  }

  function esc(str) {
    return String(str ?? '')
        .replace(/&/g, '&amp;').replace(/</g, '&lt;')
        .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }

  // ── API ───────────────────────────────────────────────────
  async function fetchById(id) {
    const res = await fetch(`${BASE_API}/events/${id}`);
    if (!res.ok) throw new Error('not found');
    return res.json();
  }

  async function fetchEvents() {
    try {
      const res = await fetch(`${BASE_API}/events`);
      if (!res.ok) throw new Error('non-2xx');
      errors = await res.json();
      renderList();
      if (errors.length) selectError(errors[0].id);
    } catch {
      demoMode = true;
      errors = SAMPLE;
      renderList();
      if (errors.length) selectError(errors[0].id);
      document.getElementById('sseLabel').textContent = 'Demo mode';
      document.getElementById('sseDot').className = 'sse-dot';
    }
  }

  // ── Render: sidebar list ──────────────────────────────────
  function renderList() {
    const list = document.getElementById('errorList');

    if (!errors.length) {
      list.innerHTML = '<div class="list-empty">No errors captured yet.<br>Waiting for events…</div>';
      return;
    }

    list.innerHTML = errors.map(e => `
      <div class="error-item${selectedId === e.id ? ' selected' : ''}" data-id="${esc(e.id)}">
        <div class="error-item-row1">
          <span class="badge badge-${esc(e.type)}">${esc(e.type.replace(/_/g, ' '))}</span>
          ${e.isNew ? '<span class="new-chip">NEW</span>' : ''}
          ${!e.aiExplanation ? '<span class="analyzing-chip">analyzing…</span>' : ''}
        </div>
        <div class="error-exc">${esc(shortClass(e.exceptionClass))}</div>
        <div class="error-time">${esc(fmtDateTime(e.timestamp))}</div>
      </div>
    `).join('');

    list.querySelectorAll('.error-item').forEach(el => {
      el.addEventListener('click', () => selectError(el.dataset.id));
    });
  }

  // ── Render: detail panel ──────────────────────────────────
  function renderDetail(e) {
    document.getElementById('emptyState').style.display = 'none';
    const panel = document.getElementById('detailPanel');
    panel.style.display = 'flex';

    const httpHtml = e.type === 'HTTP_REQUEST' ? `
      <div class="card-section">
        <div class="section-title">HTTP context</div>
        <div class="kv-grid">
          ${e.httpMethod  ? `<div class="kv-card"><div class="kv-label">method</div><div class="kv-val">${esc(e.httpMethod)}</div></div>` : ''}
          ${e.requestUri  ? `<div class="kv-card"><div class="kv-label">URI</div><div class="kv-val">${esc(e.requestUri)}</div></div>` : ''}
          ${e.requestBody ? `<div class="kv-card" style="grid-column:1/-1"><div class="kv-label">request body</div><div class="kv-val">${esc(e.requestBody)}</div></div>` : ''}
        </div>
      </div>
    ` : '';

    const ctxEntries = Object.entries(e.context || {});
    const ctxHtml = ctxEntries.length ? `
      <div class="card-section">
        <div class="section-title">context</div>
        <div class="kv-grid">
          ${ctxEntries.map(([k, v]) => `
            <div class="kv-card">
              <div class="kv-label">${esc(k)}</div>
              <div class="kv-val">${esc(v)}</div>
            </div>
          `).join('')}
        </div>
      </div>
    ` : '';

    const logsHtml = (e.recentLogs && e.recentLogs.length) ? `
      <div class="card-section">
        <div class="section-title">recent logs</div>
        <table class="logs-table">
          <thead><tr>
            <th style="width:52px">level</th>
            <th style="width:96px">logger</th>
            <th>message</th>
            <th style="width:76px">time</th>
          </tr></thead>
          <tbody>
            ${e.recentLogs.map(l => `
              <tr>
                <td><span class="log-level log-${esc(l.level)}">${esc(l.level)}</span></td>
                <td style="color:var(--text-tertiary)" title="${esc(l.loggerName)}">${esc(shortClass(l.loggerName))}</td>
                <td title="${esc(l.message)}">${esc(l.message)}</td>
                <td style="font-family:var(--font-mono);font-size:11px">${esc(fmtTime(l.timestamp))}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    ` : '';

    const aiHtml = e.aiExplanation ? `
      <div class="card">
        <div class="ai-header"><div class="ai-orb"></div>AI analysis</div>
        <div class="ai-body">
          <div class="ai-summary">${esc(e.aiExplanation.summary)}</div>
          <div>
            <div class="ai-sub">possible causes</div>
            <ul class="ai-list causes">${e.aiExplanation.causes.map(c => `<li>${esc(c)}</li>`).join('')}</ul>
          </div>
          <div>
            <div class="ai-sub">suggested fixes</div>
            <ul class="ai-list fixes">${e.aiExplanation.fixes.map(f => `<li>${esc(f)}</li>`).join('')}</ul>
          </div>
        </div>
      </div>
    ` : `
      <div class="card">
        <div class="ai-header"><div class="ai-orb"></div>AI analysis</div>
        <div class="ai-spinner"><div class="spinner"></div>Analyzing error with AI…</div>
      </div>
    `;

    panel.innerHTML = `
      <div class="card">
        <div class="detail-header">
          <div class="detail-exc">${esc(e.exceptionClass)}</div>
          <div class="detail-msg">${esc(e.message)}</div>
          <div class="detail-meta">
            <span class="badge badge-${esc(e.type)}">${esc(e.type.replace(/_/g, ' '))}</span>
            <span class="detail-ts">${esc(fmtDateTime(e.timestamp))}</span>
          </div>
        </div>
        ${httpHtml}
        ${ctxHtml}
        ${logsHtml}
      </div>
      ${aiHtml}
    `;
  }

  // ── State mutations ───────────────────────────────────────
  function selectError(id) {
    selectedId = id;
    renderList();
    const e = errors.find(x => x.id === id);
    if (e) renderDetail(e);
  }

  function addError(e) {
    errors = [{ ...e, isNew: true }, ...errors.map(x => ({ ...x, isNew: false }))];
    renderList();
    selectError(e.id);
  }

  function updateAi(id, aiExplanation) {
    errors = errors.map(e => e.id === id ? { ...e, aiExplanation } : e);
    renderList();
    if (selectedId === id) {
      const e = errors.find(x => x.id === id);
      if (e) renderDetail(e);
    }
  }

  // ── SSE ───────────────────────────────────────────────────
  function connectSSE() {
    try {
      eventSource = new EventSource(`${BASE_UI}/stream`);

      eventSource.addEventListener('error-captured', async ev => {
        const { id } = JSON.parse(ev.data);
        try {
          const fullEvent = await fetchById(id);
          addError(fullEvent);
        } catch (e) {
          console.error('Failed to fetch error by id:', id, e);
        }
      });

      eventSource.addEventListener('ai-ready', async ev => {
        const { id } = JSON.parse(ev.data);
        try {
          const fullEvent = await fetchById(id);
          updateAi(fullEvent.id, fullEvent.aiExplanation);
        } catch (e) {
          console.error('Failed to fetch ai update for id:', id, e);
        }
      });

      eventSource.onopen = () => {
        document.getElementById('sseDot').className = 'sse-dot live';
        document.getElementById('sseLabel').textContent = 'SSE connected';
      };

      eventSource.onerror = () => {
        document.getElementById('sseDot').className = 'sse-dot';
        document.getElementById('sseLabel').textContent = 'SSE disconnected';
      };

    } catch {
      // SSE not available
    }
  }

  // ── Event listeners ───────────────────────────────────────
  document.getElementById('btnClear').addEventListener('click', async () => {
    if (!demoMode) {
      await fetch(`${BASE_API}/events`, { method: 'DELETE' });
    }
    errors     = [];
    selectedId = null;
    renderList();
    document.getElementById('emptyState').style.display  = '';
    document.getElementById('detailPanel').style.display = 'none';
  });

  document.getElementById('themeToggle').addEventListener('click', () => {
    const html   = document.documentElement;
    const isDark = html.getAttribute('data-theme') === 'dark';
    html.setAttribute('data-theme', isDark ? 'light' : 'dark');
    document.getElementById('themeLabel').textContent = isDark ? 'Light' : 'Dark';
  });

  // ── Boot ──────────────────────────────────────────────────
  connectSSE();
  fetchEvents().then(r => {});

})();