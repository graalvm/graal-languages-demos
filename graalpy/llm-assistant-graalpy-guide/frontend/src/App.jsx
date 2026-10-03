import Chatbot from "./components/ChatBot";
import Sidebar from "./components/Sidebar";
function App() {
  
  return (
    <div className="min-h-screen w-full bg-gray-900 flex">
      <Sidebar />
      <div className="flex-1 flex items-center justify-center p-4">
        <Chatbot />
      </div>
    </div>
  );
}

export default App;