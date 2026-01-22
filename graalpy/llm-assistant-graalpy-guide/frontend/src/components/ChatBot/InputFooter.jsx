import React, { useState, useRef, useEffect } from "react";

export default function InputFooter({ onSendMessage, loading }) {
  const [input, setInput] = useState("");
  const inputRef = useRef(null);
  const textareaRef = useRef(null);
  
  const handleSend = () => {
    if (input.trim()) {
      onSendMessage(input);
      setInput("");
      // Reset the height after sending
      if (textareaRef.current) {
        textareaRef.current.style.height = "56px";
      }
    }
  };
  
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  // Auto-resize textarea as content grows
  const handleTextareaChange = (e) => {
    const textarea = textareaRef.current;
    setInput(e.target.value);
    
    if (textarea) {
      // Reset height to auto to get the correct scrollHeight
      textarea.style.height = "56px";
      // Set the height to the scrollHeight to fit the content
      const newHeight = Math.min(textarea.scrollHeight, 300);
      textarea.style.height = `${newHeight}px`;
    }
  };

  return (
    <footer className="p-4 bg-gray-900">
      <div>
        <div className="flex items-end bg-gray-800 rounded-xl border border-gray-700 overflow-hidden">
          <textarea
            ref={(el) => {
              inputRef.current = el;
              textareaRef.current = el;
            }}
            className="flex-1 p-4 bg-transparent focus:outline-none resize-none min-h-[56px] text-gray-100 placeholder-gray-400 overflow-y-auto"
            value={input}
            onChange={handleTextareaChange}
            placeholder="Message GraalPy..."
            onKeyDown={(e) => {
              if (e.key === "Enter" && !e.shiftKey) {
                e.preventDefault();
                handleSend();
              }
            }}
            style={{ height: "56px" }}
          />
          <button
            className="px-4 py-3 m-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 transition-colors"
            onClick={handleSend}
            disabled={loading || !input.trim()}
          >
            Send
          </button>
        </div>
        <div className="text-xs text-center text-gray-400 mt-2">
          GraalPy AI Assistant • Powered by GraalPy
        </div>
      </div>
    </footer>
  );
}