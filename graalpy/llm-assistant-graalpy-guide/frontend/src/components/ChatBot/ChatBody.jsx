import React, { useEffect, useRef } from "react";
import { ClipLoader } from "react-spinners";
import logo from "../../assets/logo.svg";

export default function ChatBody({ 
  messages = [], 
  typingText, 
  loading, 
  messagesEndRef 
}) {
  // Create a local ref for the typing container
  const typingContainerRef = useRef(null);

  // Improved scrolling during typing effect that handles code blocks better
  useEffect(() => {
    if (typingText && messagesEndRef.current) {
      requestAnimationFrame(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
      });
    }
  }, [typingText, messagesEndRef]); // Added messagesEndRef to dependencies

  // Handle special cases when code blocks or tables appear/change
  const lastTypingContentRef = useRef("");

  useEffect(() => {
    const hasNewCodeBlock =
      typingText &&
      (typingText.includes("<pre") || typingText.includes("<table")) &&
      (!lastTypingContentRef.current.includes("<pre") ||
        !lastTypingContentRef.current.includes("<table"));

    if (hasNewCodeBlock && messagesEndRef.current) {
      setTimeout(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
      }, 50);
    }

    lastTypingContentRef.current = typingText || "";
  }, [typingText, messagesEndRef]); // Added messagesEndRef to dependencies

  const formatUserMessage = (text) => {
    const isLongMessage = text.length > 200;

    return (
      <div className={`user-message ${isLongMessage ? "break-words whitespace-pre-wrap" : ""}`}>
        {text}
      </div>
    );
  };

  return (
    <main className="flex-1 overflow-y-auto p-6 bg-transparent dark:bg-transparent">
      <div className="space-y-6">
        {messages.map((msg, index) => (
          <div key={index} className={`flex ${msg.sender === "user" ? "justify-end" : "justify-start"}`}>
            {msg.sender === "bot" && (
              <div className="flex-shrink-0 mr-3 flex">
                <img src={logo} alt="Bot" className="w-8 h-8 mt-4" />
              </div>
            )}
            <div
              className={`max-w-[85%] p-4 rounded-2xl ${
                msg.sender === "user"
                  ? "bg-gray-700 border border-gray-700 text-gray-100"
                  : "bg-transparent border border-transparent text-gray-100"
              }`}
            >
              {msg.sender === "bot" ? (
                <div className="prose dark:prose-invert prose-sm max-w-none overflow-x-auto" 
                  dangerouslySetInnerHTML={{ __html: msg.text }} />
              ) : (
                formatUserMessage(msg.text)
              )}
            </div>
          </div>
        ))}

        {/* Loading Indicator */}
        {loading && (
          <div className="flex justify-start">
            <div className="flex-shrink-0 mr-3 flex items-center">
              <img src={logo} alt="Bot" className="w-8 h-8" />
            </div>
            <div className="max-w-[85%] p-4 rounded-2xl bg-transparent border border-transparent text-gray-100 flex items-center space-x-3">
              <ClipLoader size={16} color="currentColor" />
              <span className="text-sm">Thinking...</span>
            </div>
          </div>
        )}

        {/* Typing Effect with improved handling for code/tables */}
        {typingText && (
          <div className="flex justify-start" ref={typingContainerRef}>
            <div className="flex-shrink-0 mr-3 flex">
              <img src={logo} alt="Bot" className="w-8 h-8 mt-4" />
            </div>
            <div className="max-w-[85%] p-4 rounded-2xl bg-transparent border border-transparent text-gray-100 min-h-[3rem]">
              <div className="prose dark:prose-invert prose-sm max-w-none overflow-x-auto" 
                dangerouslySetInnerHTML={{ __html: typingText }} />
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>
    </main>
  );
}
