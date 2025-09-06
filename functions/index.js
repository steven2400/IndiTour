// functions/index.js
const functions = require("firebase-functions/v2");
const { onCall } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const axios = require("axios");

// 🔑 Define secret
const OPENAI_API_KEY = defineSecret("OPENAI_API_KEY");

exports.generateItinerary = onCall(
  { secrets: [OPENAI_API_KEY] },
  async (request) => {
    const { destination, days, interests } = request.data;

    try {
      const response = await axios.post(
        "https://api.openai.com/v1/chat/completions",
        {
          model: "gpt-3.5-turbo",
          messages: [
            {
              role: "system",
              content: "You are a travel planner. Create detailed day-by-day itineraries.",
            },
            {
              role: "user",
              content: `Plan a ${days}-day trip to ${destination} focusing on ${interests}. Include hotels, attractions, and food.`,
            },
          ],
        },
        {
          headers: {
            Authorization: `Bearer ${OPENAI_API_KEY.value()}`, // do not add extra quotes
            "Content-Type": "application/json",
          },
        }
      );

      const itinerary = response.data.choices[0].message?.content || "No itinerary generated";
      return { itinerary };

    } catch (error) {
      // 🔹 Debug logs
      console.error("Full error object:", error);
      console.error("Response data:", error.response?.data);
      console.error("Error message:", error.message);

      // 🔹 Specific handling for quota exceeded
      if (error.response?.data?.error?.code === "insufficient_quota") {
        throw new functions.https.HttpsError(
          "resource-exhausted",
          "OpenAI API quota exceeded. Please check your plan and billing."
        );
      }

      // 🔹 Return the original error message for debugging
      throw new functions.https.HttpsError(
        "internal",
        error.response?.data?.error?.message || error.message
      );
    }
  }
);
