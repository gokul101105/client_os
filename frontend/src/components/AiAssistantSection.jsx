import { useState } from 'react';
import { askQuestion } from '../services/aiService';

const SUGGESTED_QUESTIONS = [
  'Summarize this client',
  'What problems have they reported?',
  'What features do they want?',
];

// Chat history here is React state only — gone on refresh, never sent
// anywhere to persist. It's also *display* history only: each question
// below is still a completely independent, memory-free call to the RAG
// pipeline (Module 11 takes no prior turns), so a follow-up question has
// no awareness of earlier ones in this same list.
export default function AiAssistantSection({ clientId }) {
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState([]);
  const [asking, setAsking] = useState(false);
  const [error, setError] = useState('');

  const submitQuestion = async (text) => {
    const trimmed = text.trim();
    if (!trimmed || asking) return;

    setError('');
    setAsking(true);
    setQuestion('');
    try {
      const result = await askQuestion(clientId, trimmed);
      setMessages((prev) => [
        ...prev,
        {
          id: crypto.randomUUID(),
          question: trimmed,
          answer: result.reply,
          sourceChunkIds: result.sourceChunkIds ?? [],
        },
      ]);
    } catch (err) {
      const message =
        err.code === 'ECONNABORTED'
          ? 'The AI assistant is taking too long to respond. Please try again.'
          : err.response?.data?.message || 'Something went wrong asking the AI assistant.';
      setError(message);
    } finally {
      setAsking(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    submitQuestion(question);
  };

  return (
    <div>
      <div className="flex flex-wrap gap-2">
        {SUGGESTED_QUESTIONS.map((suggestion) => (
          <button
            key={suggestion}
            type="button"
            onClick={() => submitQuestion(suggestion)}
            disabled={asking}
            className="rounded-full border border-gray-300 px-3 py-1 text-xs font-medium text-gray-600 hover:bg-gray-50 disabled:opacity-50"
          >
            {suggestion}
          </button>
        ))}
      </div>

      <form onSubmit={handleSubmit} className="mt-4 flex gap-2">
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          placeholder="Ask something about this client…"
          disabled={asking}
          className="flex-1 rounded-md border border-gray-300 px-3 py-2 text-sm focus:border-blue-500 focus:outline-none focus:ring-1 focus:ring-blue-500"
        />
        <button
          type="submit"
          disabled={asking || !question.trim()}
          className="rounded-md bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:opacity-60"
        >
          {asking ? 'Asking…' : 'Ask'}
        </button>
      </form>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

      <div className="mt-6 space-y-4">
        {messages.length === 0 && !asking && (
          <p className="text-sm text-gray-500">
            Ask a question above to get an answer grounded in this client's uploaded documents.
          </p>
        )}

        {messages.map((msg) => (
          <div key={msg.id} className="rounded-lg border border-gray-200 bg-white p-4">
            <p className="text-sm font-medium text-gray-900">{msg.question}</p>
            <p className="mt-2 whitespace-pre-wrap text-sm text-gray-700">{msg.answer}</p>
            {msg.sourceChunkIds.length > 0 && (
              <p className="mt-2 text-xs text-gray-400">
                Based on {msg.sourceChunkIds.length} excerpt{msg.sourceChunkIds.length === 1 ? '' : 's'} from
                this client's documents.
              </p>
            )}
          </div>
        ))}

        {asking && (
          <div className="rounded-lg border border-gray-200 bg-white p-4">
            <p className="text-sm text-gray-500">Thinking…</p>
          </div>
        )}
      </div>
    </div>
  );
}
