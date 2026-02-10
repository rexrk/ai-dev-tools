/**
 * Swagger UI Plugin — "🤖 AI Generate Body" button
 * Calls Spring Boot backend for AI generation
 */
const AiGenerateBodyPlugin = (system) => {
    const React = system.React;

    if (!React) {
        console.error("❌ React is not available in the plugin system");
        return {};
    }

    console.log("✅ AiGenerateBodyPlugin initializing");

    // Resolve $ref in schema
    function resolveSchema(schema, specJson, depth = 0) {
        if (!schema || depth > 10) return schema;

        if (schema.$ref) {
            const parts = schema.$ref.split("/");
            let resolved = specJson;
            for (let i = 1; i < parts.length; i++) {
                if (!resolved[parts[i]]) return schema;
                resolved = resolved[parts[i]];
            }
            return resolveSchema(resolved, specJson, depth + 1);
        }

        // Recursively resolve nested schemas
        if (schema.properties) {
            const resolvedProps = {};
            for (const [key, value] of Object.entries(schema.properties)) {
                resolvedProps[key] = resolveSchema(value, specJson, depth + 1);
            }
            return { ...schema, properties: resolvedProps };
        }

        if (schema.items) {
            return { ...schema, items: resolveSchema(schema.items, specJson, depth + 1) };
        }

        return schema;
    }

    return {
        wrapComponents: {
            RequestBodyEditor: (Original, system) => (props) => {
                const { onChange } = props;
                const [loading, setLoading] = React.useState(false);

                const handleGenerate = async () => {
                    try {
                        setLoading(true);

                        const specSelectors = system.specSelectors;
                        const spec = specSelectors.specJson();
                        const specJson = spec ? (spec.toJS ? spec.toJS() : spec) : {};

                        // Find schema
                        const paths = specJson.paths || {};
                        let foundSchema = null;

                        for (const [pathKey, pathItem] of Object.entries(paths)) {
                            for (const [methodKey, operation] of Object.entries(pathItem)) {
                                if (typeof operation === 'object' && operation.requestBody) {
                                    const content = operation.requestBody.content || {};
                                    const mediaType = Object.keys(content).find(k => k.includes('json'));
                                    if (mediaType && content[mediaType].schema) {
                                        foundSchema = content[mediaType].schema;
                                        break;
                                    }
                                }
                            }
                            if (foundSchema) break;
                        }

                        if (!foundSchema) {
                            alert("No JSON request body schema found");
                            return;
                        }

                        // Resolve all $ref in the schema
                        const resolvedSchema = resolveSchema(foundSchema, specJson);
                        console.log("📝 Resolved schema:", resolvedSchema);

                        // Call Spring Boot backend
                        const response = await fetch("/ai-swagger/generate", {
                            method: "POST",
                            headers: { "Content-Type": "application/json" },
                            body: JSON.stringify({ schema: resolvedSchema })
                        });

                        const result = await response.json();

                        if (result.success && onChange) {
                            onChange(result.body);
                            console.log("✅ Generated body");
                        } else {
                            alert("Error: " + (result.error || "Unknown error"));
                        }

                    } catch (error) {
                        console.error("❌ Error:", error);
                        alert("Error: " + error.message);
                    } finally {
                        setLoading(false);
                    }
                };

                return React.createElement("div", null,
                    React.createElement("div", {
                            style: {
                                display: "flex",
                                justifyContent: "flex-end",
                                marginBottom: "8px",
                                padding: "4px 0"
                            }
                        },
                        React.createElement("button", {
                            type: "button",
                            disabled: loading,
                            style: {
                                cursor: loading ? "wait" : "pointer",
                                padding: "6px 14px",
                                borderRadius: "4px",
                                border: "2px solid #49cc90",
                                background: loading ? "#e0e0e0" : "#fff",
                                color: loading ? "#999" : "#49cc90",
                                fontWeight: "700",
                                fontSize: "14px",
                                opacity: loading ? 0.6 : 1
                            },
                            onMouseOver: (e) => {
                                if (!loading) {
                                    e.target.style.background = "#49cc90";
                                    e.target.style.color = "#fff";
                                }
                            },
                            onMouseOut: (e) => {
                                if (!loading) {
                                    e.target.style.background = "#fff";
                                    e.target.style.color = "#49cc90";
                                }
                            },
                            onClick: handleGenerate
                        }, loading ? "🤖 cooking JSON…" : "🤖 AI pls, body")
                    ),
                    React.createElement(Original, props)
                );
            }
        }
    };
};

window.AiGenerateBodyPlugin = AiGenerateBodyPlugin;
console.log("✅ AiGenerateBodyPlugin loaded");