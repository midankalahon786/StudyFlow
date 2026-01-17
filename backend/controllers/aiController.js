const { GoogleGenerativeAI } = require("@google/generative-ai");
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY);

const MODELS_TO_TRY = [
    "gemini-flash-latest",
    "gemini-pro-latest",
    "gemini-2.0-flash-lite-preview-02-05"
];

exports.generateQuizQuestions = async (req, res) => {
    const { topic, count } = req.body;

    if (!topic) {
        return res.status(400).json({ error: "Topic is required" });
    }

    const prompt = `
    You are a strict JSON generator for a Learning Management System.
    Generate ${count || 5} multiple-choice questions about "${topic}" at an intermediate level.
    
    Output format must be a strictly valid JSON array of objects with this structure:
    [
      {
        "questionText": "Question string?",
        "options": ["Option A", "Option B", "Option C", "Option D"],
        "correctOptionIndex": 0,
        "mark": 1
      }
    ]
    Do not include markdown formatting (like \`\`\`json). Just the raw JSON array.
  `;

    let lastError = null;

    for (const modelName of MODELS_TO_TRY) {
        try {
            console.log(`Attempting generation with model: ${modelName}...`);
            const model = genAI.getGenerativeModel({ model: modelName });

            const result = await model.generateContent(prompt);
            const response = await result.response;

            let text = response.text();
            text = text.replace(/```json/g, "").replace(/```/g, "").trim();

            const questions = JSON.parse(text);

            console.log(`Success with ${modelName}!`);
            return res.json({ success: true, data: questions });

        } catch (error) {
            console.error(`Failed with ${modelName}:`, error.message);
            lastError = error;
        }
    }

    console.error("All AI models failed.");
    res.status(500).json({
        error: "AI service unavailable. Please try again later.",
        details: lastError ? lastError.message : "Unknown error"
    });
};