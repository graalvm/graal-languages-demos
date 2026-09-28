import React, { useState, useEffect } from "react";
import "./sidebar.css";
import Swal from "sweetalert2";

export default function Sidebar({ onUrlSubmit, onTextSubmit }) {
  // State management
  const [url, setUrl] = useState("");
  const [additionalText, setAdditionalText] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isTextLoading, setIsTextLoading] = useState(false);
  const [savedUrls, setSavedUrls] = useState(() => {
    const cachedUrls = localStorage.getItem("chatbot-saved-urls");
    return cachedUrls ? JSON.parse(cachedUrls) : [];
  });
  const [expanded, setExpanded] = useState(() => {
    const cachedExpanded = localStorage.getItem("sidebar-expanded");
    return cachedExpanded !== null ? JSON.parse(cachedExpanded) : true;
  });

  // LocalStorage sync effects (only for URLs and sidebar expansion state)
  useEffect(() => {
    localStorage.setItem("chatbot-saved-urls", JSON.stringify(savedUrls));
  }, [savedUrls]);

  useEffect(() => {
    localStorage.setItem("sidebar-expanded", JSON.stringify(expanded));
  }, [expanded]);

  // Initial data notification effects
  useEffect(() => {
    if (savedUrls.length > 0 && onUrlSubmit) {
      onUrlSubmit(null, savedUrls);
    }
  }, [savedUrls, onUrlSubmit]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!url.trim()) return;

    const formattedUrl = url.startsWith("http") ? url : `https://${url}`;

    if (savedUrls.includes(formattedUrl)) {
      Swal.fire({
        icon: "warning",
        title: "URL Already Saved",
        text: "This URL is already saved. Please try another one.",
        confirmButtonText: "OK",
      });
      return;
    }

    try {
      new URL(formattedUrl);
      setIsLoading(true);

      const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/api/llm/add-url`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url: formattedUrl }),
      });

      const result = await response.json();
      setIsLoading(false);

      if (!response.ok) {
        Swal.fire({
          icon: "error",
          title: "Invalid URL",
          text: result.message || "An error occurred while adding the URL.",
          confirmButtonText: "OK",
        });
        return;
      }

      const newUrls = [...savedUrls, formattedUrl];
      setSavedUrls(newUrls);
      if (onUrlSubmit) onUrlSubmit(formattedUrl, newUrls);
      setUrl("");

      Swal.fire({
        icon: "success",
        title: "Success",
        text: result.message || "The URL has been successfully added.",
        confirmButtonText: "OK",
      });
    } catch (error) {
      setIsLoading(false);
      console.error("Error adding URL:", error);
      Swal.fire({
        icon: "error",
        title: "Invalid URL",
        text: "Please enter a valid URL.",
        confirmButtonText: "OK",
      });
    }
  };

  const handleDelete = (index) => {
    const newUrls = savedUrls.filter((_, i) => i !== index);
    setSavedUrls(newUrls);
    if (onUrlSubmit) onUrlSubmit(null, newUrls);
  };

  const clearAllUrls = () => {
    Swal.fire({
      title: "Are you sure?",
      text: "This will remove all saved URLs.",
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#3085d6",
      confirmButtonText: "Yes, clear all!",
    }).then((result) => {
      if (result.isConfirmed) {
        setSavedUrls([]);
        if (onUrlSubmit) onUrlSubmit(null, []);
        Swal.fire("Cleared!", "All URLs have been removed.", "success");
      }
    });
  };

  // Text handlers
  const handleTextSubmit = async (e) => {
    e.preventDefault();
    if (!additionalText.trim()) return;

    try {
      setIsTextLoading(true);
      const response = await fetch(`${process.env.REACT_APP_BACKEND_URL}/api/llm/add-text`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: additionalText }),
      });

      const result = await response.json();
      setIsTextLoading(false);

      if (!response.ok) {
        Swal.fire({
          icon: "error",
          title: "Error",
          text: result.message || "An error occurred while adding the text.",
          confirmButtonText: "OK",
        });
        return;
      }

      if (onTextSubmit) onTextSubmit(additionalText);
      
      Swal.fire({
        icon: "success",
        title: "Success",
        text: result.message || "Your text has been successfully added.",
        confirmButtonText: "OK",
      });

      setAdditionalText("");
    } catch (error) {
      setIsTextLoading(false);
      console.error("Error adding text:", error);
      Swal.fire({
        icon: "error",
        title: "Error",
        text: "An error occurred while adding your text.",
        confirmButtonText: "OK",
      });
    }
  };

  // UI toggle
  const toggleSidebar = () => setExpanded((prev) => !prev);

  // Render URL list item
  const renderUrlItem = (savedUrl, index) => {
    try {
      const urlObj = new URL(savedUrl);
      const displayUrl = `${urlObj.hostname}${urlObj.pathname !== "/" ? urlObj.pathname : ""}`;

      return (
        <li key={index} className="url-item">
          <div className="url-item-content">
            <a href={savedUrl} target="_blank" rel="noopener noreferrer" className="url-link" title={savedUrl}>
              {displayUrl}
            </a>
            <button onClick={() => handleDelete(index)} className="url-delete-button" aria-label="Delete URL">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </li>
      );
    } catch {
      return (
        <li key={index} className="url-item url-item-error">
          <div className="url-item-content">
            <span className="url-error-text">Invalid URL</span>
            <button onClick={() => handleDelete(index)} className="url-delete-button" aria-label="Delete URL">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"></line>
                <line x1="6" y1="6" x2="18" y2="18"></line>
              </svg>
            </button>
          </div>
        </li>
      );
    }
  };

  // Collapsed sidebar view
  if (!expanded) {
    return (
      <div className="sidebar-container collapsed">
        <div className="sidebar-icon-container" onClick={toggleSidebar}>
          <div className="sidebar-icon" aria-label="Expand sidebar">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 18l6-6-6-6" />
            </svg>
          </div>
          {(savedUrls.length > 0 || additionalText) && (
            <div className="resources-badge" title={`${savedUrls.length} URLs${additionalText ? ' + Text' : ''}`}>
              {savedUrls.length > 0 ? savedUrls.length : ''}
              {additionalText ? '+' : ''}
            </div>
          )}
        </div>
      </div>
    );
  }

  // Expanded sidebar view
  return (
    <div className="sidebar-container expanded">
      <div className="sidebar-toggle" onClick={toggleSidebar} aria-label="Collapse sidebar">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M15 18l-6-6 6-6" />
        </svg>
      </div>

      <div className="sidebar-content">
        <h2 className="sidebar-title">Additional Resources</h2>
        
        {/* URL Input Section */}
        <section className="sidebar-section url-input-section">
          <form onSubmit={handleSubmit} className="url-form">
            <label htmlFor="url-input" className="url-label">
              Add a URL for context
            </label>
            <div className="url-input-container">
              <input
                id="url-input"
                type="text"
                value={url}
                onChange={(e) => setUrl(e.target.value)}
                placeholder="Enter URL"
                className="url-input"
                disabled={isLoading}
              />
              <button 
                type="submit" 
                className={`url-submit-button ${isLoading ? 'loading' : ''}`} 
                disabled={!url.trim() || isLoading}
              >
                {isLoading ? (
                  <svg className="loading-spinner" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                    <circle className="spinner-path" cx="12" cy="12" r="10" fill="none" strokeWidth="4"></circle>
                  </svg>
                ) : "Add"}
              </button>
            </div>
          </form>
        </section>

        {/* Saved URLs Section */}
        <section className="sidebar-section saved-urls-section">
          <div className="url-list-header">
            <h3 className="section-title">Saved URLs</h3>
            {savedUrls.length > 0 && (
              <button onClick={clearAllUrls} className="clear-all-button" aria-label="Clear all URLs">
                Clear All
              </button>
            )}
          </div>

          <div className="url-list-container">
            {savedUrls.length === 0 ? (
              <p className="no-urls-message">No URLs added yet</p>
            ) : (
              <ul className="url-list">
                {savedUrls.map((savedUrl, index) => renderUrlItem(savedUrl, index))}
              </ul>
            )}
          </div>
        </section>

        {/* Additional Text Section */}
        <section className="sidebar-section text-input-section">
          <div className="section-divider"></div>
          
          <div className="text-input-header">
            <h3 className="section-title">Additional Text</h3>
          </div>
          
          <form onSubmit={handleTextSubmit} className="text-form">
            <textarea
              id="text-input"
              value={additionalText}
              onChange={(e) => setAdditionalText(e.target.value)}
              placeholder="Add additional text context here..."
              className="text-input"
              disabled={isTextLoading}
              rows="5"
            />
            <button 
              type="submit" 
              className={`text-submit-button ${isTextLoading ? 'loading' : ''}`} 
              disabled={!additionalText.trim() || isTextLoading}
            >
              {isTextLoading ? (
                <svg className="loading-spinner" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <circle className="spinner-path" cx="12" cy="12" r="10" fill="none" strokeWidth="4"></circle>
                </svg>
              ) : "Save Text"}
            </button>
          </form>
        </section>
      </div>
    </div>
  );
}