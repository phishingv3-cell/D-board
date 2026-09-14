import React, { useState, useRef, useEffect } from 'react';
import { Keyboard } from './components/Keyboard';
import { getSuggestions } from './data/dictionary';

export default function App() {
  const [inputText, setInputText] = useState<string>('');
  const [soundEnabled, setSoundEnabled] = useState<boolean>(true);
  const [hapticEnabled, setHapticEnabled] = useState<boolean>(true);
  const [copied, setCopied] = useState<boolean>(false);
  const [feedbackToast, setFeedbackToast] = useState<string | null>(null);
  const [suggestions, setSuggestions] = useState<string[]>(['I', "Let's", 'The']);
  const [selectionRange, setSelectionRange] = useState<{ start: number; end: number }>({ start: 0, end: 0 });

  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Show small visual feedback toast
  const showToast = (msg: string) => {
    setFeedbackToast(msg);
    setTimeout(() => setFeedbackToast(null), 1600);
  };

  // Update suggestions whenever text changes
  useEffect(() => {
    const list = getSuggestions(inputText);
    setSuggestions(list);
  }, [inputText]);

  const updateSelectionState = () => {
    if (textareaRef.current) {
      setSelectionRange({
        start: textareaRef.current.selectionStart,
        end: textareaRef.current.selectionEnd,
      });
    }
  };

  // Insert character or emoji with selection replacement
  const handleInsertText = (text: string) => {
    const el = textareaRef.current;
    if (el) {
      const start = el.selectionStart;
      const end = el.selectionEnd;
      const nextText = inputText.substring(0, start) + text + inputText.substring(end);
      setInputText(nextText);
      const newPos = start + text.length;
      requestAnimationFrame(() => {
        el.focus();
        el.setSelectionRange(newPos, newPos);
        setSelectionRange({ start: newPos, end: newPos });
      });
    } else {
      setInputText((prev) => prev + text);
    }
  };

  // Backspace: if text is selected, clears the selection! (Select all + backspace = clear)
  const handleBackspace = () => {
    const el = textareaRef.current;
    if (el) {
      const start = el.selectionStart;
      const end = el.selectionEnd;

      // When text is selected, backspace deletes the selection!
      if (start !== end) {
        const nextText = inputText.substring(0, start) + inputText.substring(end);
        setInputText(nextText);
        requestAnimationFrame(() => {
          el.focus();
          el.setSelectionRange(start, start);
          setSelectionRange({ start, end: start });
        });
        return;
      }

      // Single character / emoji delete before cursor
      if (start > 0) {
        // Account for emoji/surrogate pairs
        const textBefore = inputText.substring(0, start);
        const chars = Array.from(textBefore);
        chars.pop();
        const newTextBefore = chars.join('');
        const deletedLength = textBefore.length - newTextBefore.length;
        const nextText = newTextBefore + inputText.substring(start);
        const newPos = start - deletedLength;

        setInputText(nextText);
        requestAnimationFrame(() => {
          el.focus();
          el.setSelectionRange(newPos, newPos);
          setSelectionRange({ start: newPos, end: newPos });
        });
        return;
      }
    }

    // Fallback
    setInputText((prev) => {
      if (prev.length === 0) return '';
      const chars = Array.from(prev);
      chars.pop();
      return chars.join('');
    });
  };

  // Enter / newline
  const handleEnter = () => {
    handleInsertText('\n');
  };

  // Clear text
  const handleClear = () => {
    setInputText('');
    setSelectionRange({ start: 0, end: 0 });
    showToast('Cleared');
    if (textareaRef.current) {
      textareaRef.current.focus();
    }
  };

  // Select All: highlights entire text in the textarea
  const handleSelectAll = () => {
    if (!inputText) return;
    const el = textareaRef.current;
    if (el) {
      el.focus();
      el.setSelectionRange(0, inputText.length);
      setSelectionRange({ start: 0, end: inputText.length });
      showToast('All Selected (Press ⌫ to Clear)');
    }
  };

  // Copy selected text or all text to clipboard
  const handleCopy = async () => {
    const el = textareaRef.current;
    let textToCopy = inputText;
    if (el && el.selectionStart !== el.selectionEnd) {
      textToCopy = inputText.substring(el.selectionStart, el.selectionEnd);
    }
    if (!textToCopy) return;

    try {
      await navigator.clipboard.writeText(textToCopy);
      setCopied(true);
      showToast('Copied to clipboard');
      setTimeout(() => setCopied(false), 1800);
    } catch {
      // Fallback
      showToast('Copied');
    }
  };

  // Cut selected text or all text to clipboard
  const handleCut = async () => {
    const el = textareaRef.current;
    if (!inputText) return;

    let textToCut = inputText;
    let start = 0;
    let end = inputText.length;

    if (el && el.selectionStart !== el.selectionEnd) {
      start = el.selectionStart;
      end = el.selectionEnd;
      textToCut = inputText.substring(start, end);
    }

    try {
      await navigator.clipboard.writeText(textToCut);
    } catch {
      // Ignore clipboard write failure
    }

    const nextText = inputText.substring(0, start) + inputText.substring(end);
    setInputText(nextText);
    showToast('Cut to clipboard');

    requestAnimationFrame(() => {
      if (el) {
        el.focus();
        el.setSelectionRange(start, start);
        setSelectionRange({ start, end: start });
      }
    });
  };

  // Paste from clipboard into text at cursor position
  const handlePaste = async () => {
    try {
      const clipText = await navigator.clipboard.readText();
      if (clipText) {
        handleInsertText(clipText);
        showToast('Pasted');
      }
    } catch {
      // In case browser denies permission, prompt or inform user
      const manual = prompt('Paste your text here:');
      if (manual) {
        handleInsertText(manual);
        showToast('Pasted');
      }
    }
  };

  // Click on a word suggestion
  const handleSelectSuggestion = (word: string) => {
    setInputText((prev) => {
      if (!prev || prev.trim().length === 0) {
        return word + ' ';
      }
      const words = prev.trimEnd().split(/\s+/);
      if (prev.endsWith(' ')) {
        return prev + word + ' ';
      }
      words[words.length - 1] = word;
      return words.join(' ') + ' ';
    });
  };

  const hasSelection = selectionRange.start !== selectionRange.end;
  const hasText = inputText.length > 0;

  return (
    <main
      id="xboard-app-root"
      className="w-full min-h-screen bg-[#0A0B0E] text-white flex flex-col items-center justify-between"
    >
      {/* Top App Header */}
      <header className="w-full max-w-xl flex items-center justify-between px-4 py-3 border-b border-[#1D2028] bg-[#111216]/80 backdrop-blur-sm">
        <div className="flex items-center gap-2.5">
          <div className="w-7 h-7 rounded-lg bg-[#00DF6C] flex items-center justify-center font-bold text-[#052413] text-sm shadow-[0_0_12px_rgba(0,223,108,0.3)]">
            X
          </div>
          <div>
            <h1 className="text-sm font-bold tracking-wide text-white leading-tight">
              X BOARD
            </h1>
            <p className="text-[11px] text-[#00DF6C] font-mono leading-none">
              Android 8.0+ Keyboard
            </p>
          </div>
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2">
          {/* Sound Toggle */}
          <button
            id="btn-toggle-sound"
            type="button"
            onClick={() => setSoundEnabled((prev) => !prev)}
            className={`p-2 rounded-lg text-xs font-medium border transition cursor-pointer ${
              soundEnabled
                ? 'bg-[#00DF6C]/10 border-[#00DF6C]/40 text-[#00DF6C]'
                : 'bg-[#1C1E26] border-[#2B2F3D] text-gray-400'
            }`}
            title={soundEnabled ? 'Key Sound: On' : 'Key Sound: Off'}
            aria-label="Toggle Key Sound"
          >
            {soundEnabled ? (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M15.536 8.464a5 5 0 010 7.072m2.828-9.9a9 9 0 010 12.728M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
              </svg>
            ) : (
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M5.586 15H4a1 1 0 01-1-1v-4a1 1 0 011-1h1.586l4.707-4.707C10.923 3.663 12 4.109 12 5v14c0 .891-1.077 1.337-1.707.707L5.586 15z" />
                <path strokeLinecap="round" strokeLinejoin="round" d="M17 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2" />
              </svg>
            )}
          </button>

          {/* Copy Text */}
          <button
            id="btn-copy-text"
            type="button"
            onClick={handleCopy}
            disabled={!inputText}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-[#1F222C] hover:bg-[#282C38] text-gray-200 border border-[#2B2F3D] disabled:opacity-40 transition cursor-pointer active:scale-95"
          >
            {copied ? (
              <>
                <svg className="w-3.5 h-3.5 text-[#00DF6C]" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                </svg>
                <span className="text-[#00DF6C]">Copied</span>
              </>
            ) : (
              <>
                <svg className="w-3.5 h-3.5 text-gray-300" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                </svg>
                <span>Copy</span>
              </>
            )}
          </button>
        </div>
      </header>

      {/* Toast message if active */}
      {feedbackToast && (
        <div className="fixed top-16 z-50 px-4 py-2 rounded-lg bg-[#00DF6C] text-[#052413] font-semibold text-xs shadow-lg animate-bounce">
          {feedbackToast}
        </div>
      )}

      {/* Typing Display Screen */}
      <section className="w-full max-w-xl flex-1 flex flex-col p-4">
        <div className="w-full flex-1 min-h-[160px] bg-[#14161D] rounded-xl border border-[#222530] p-4 flex flex-col justify-between shadow-inner focus-within:border-[#00DF6C]/50 transition-colors">
          <textarea
            ref={textareaRef}
            id="typing-textarea"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            onSelect={updateSelectionState}
            onKeyUp={updateSelectionState}
            onClick={updateSelectionState}
            placeholder="Type anything using X Board below..."
            className="w-full flex-1 bg-transparent text-white text-lg sm:text-xl font-normal resize-none focus:outline-none placeholder-[#545B6D] leading-relaxed select-text"
          />

          <div className="flex items-center justify-between pt-2 border-t border-[#1C1F28] text-xs text-gray-400">
            <div className="flex items-center gap-3">
              <span className="font-mono">{inputText.length} characters</span>
              {hasSelection && (
                <span className="text-[#00DF6C] font-mono text-[11px]">
                  ({selectionRange.end - selectionRange.start} selected)
                </span>
              )}
            </div>
            {inputText && (
              <button
                id="btn-clear-all"
                type="button"
                onClick={handleClear}
                className="text-xs text-[#E55353] hover:text-red-400 font-medium cursor-pointer"
              >
                Clear all
              </button>
            )}
          </div>
        </div>
      </section>

      {/* The Keyboard exactly matching the user's photo */}
      <footer className="w-full max-w-xl flex flex-col">
        <Keyboard
          onInsertText={handleInsertText}
          onBackspace={handleBackspace}
          onEnter={handleEnter}
          onClear={handleClear}
          onSelectAll={handleSelectAll}
          onCopy={handleCopy}
          onCut={handleCut}
          onPaste={handlePaste}
          hasSelection={hasSelection}
          hasText={hasText}
          suggestions={suggestions}
          onSelectSuggestion={handleSelectSuggestion}
          soundEnabled={soundEnabled}
          hapticEnabled={hapticEnabled}
        />
      </footer>
    </main>
  );
}
