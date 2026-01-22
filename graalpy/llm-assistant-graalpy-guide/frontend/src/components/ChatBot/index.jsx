import { useState, useRef, useEffect } from "react";
import { marked } from "marked";
import DOMPurify from "dompurify";
import hljs from "highlight.js";
import "highlight.js/styles/github-dark.css";
import "./chat.css";

import Header from "./Header";
import ChatBody from "./ChatBody";
import InputFooter from "./InputFooter";

marked.setOptions({
  highlight: (code, lang) => {
    const validLang = hljs.getLanguage(lang) ? lang : "plaintext";
    return hljs.highlight(code, { language: validLang }).value;
  },
});

export default function Chatbot() {
  const [messages, setMessages] = useState([
    { text: "Hello! How can I assist you today?😊", sender: "bot" }
  ]);
  const [loading, setLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const sendMessage = async (messageText) => {
    if (!messageText.trim()) return;

    const userMessage = { text: messageText, sender: "user" };
    setMessages((prev) => [...prev, userMessage]);
    setLoading(true);

    try {
      const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/api/llm/answer`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ query: messageText }),
      });

      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }

      const textResponse = await response.text();

      const markdown = marked(textResponse);
      const sanitizedMarkdown = DOMPurify.sanitize(markdown);

      setMessages((prev) => [...prev, { text: sanitizedMarkdown, sender: "bot" }]);
    } catch (error) {
      console.error("Error fetching chatbot response:", error);
      setMessages((prev) => [
        ...prev,
        { text: "I apologize, but I'm having trouble connecting to the server. Please try again later.", sender: "bot" },
      ]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const highlightCodeBlocks = () => {
      document.querySelectorAll("pre code:not(.hljs)").forEach((block) => {
        hljs.highlightElement(block);
      });
    };

    const timeoutId = setTimeout(highlightCodeBlocks, 50);

    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });

    return () => clearTimeout(timeoutId);
  }, [messages]);

  return (
    <div className="flex flex-col w-full max-w-4xl h-screen rounded-lg overflow-hidden bg-transparent">
      <Header />

      <ChatBody 
        messages={messages}
        loading={loading}
        messagesEndRef={messagesEndRef}
      />

      <InputFooter 
        onSendMessage={sendMessage}
        loading={loading}
      />
    </div>
  );
}
