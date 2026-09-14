import React, { useState, useRef, useEffect, useCallback } from 'react';
import { ShiftState, KeyboardMode } from '../types';
import { LETTER_ROWS, SYMBOL_ROWS, MORE_SYMBOL_ROWS, POPULAR_EMOJIS } from '../data/layouts';
import { playKeySound } from '../utils/audio';

interface KeyboardProps {
  onInsertText: (text: string) => void;
  onBackspace: () => void;
  onEnter: () => void;
  onClear: () => void;
  onSelectAll?: () => void;
  onCopy?: () => void;
  onCut?: () => void;
  onPaste?: () => void;
  hasSelection?: boolean;
  hasText?: boolean;
  suggestions: string[];
  onSelectSuggestion: (word: string) => void;
  soundEnabled: boolean;
  hapticEnabled: boolean;
}

export const Keyboard: React.FC<KeyboardProps> = ({
  onInsertText,
  onBackspace,
  onEnter,
  onClear,
  onSelectAll,
  onCopy,
  onCut,
  onPaste,
  hasSelection = false,
  hasText = false,
  suggestions,
  onSelectSuggestion,
  soundEnabled,
  hapticEnabled,
}) => {
  // Shift state: 'shift' is initially active matching the user's screenshot where letters are CAPITAL!
  const [shiftState, setShiftState] = useState<ShiftState>('shift');
  const [mode, setMode] = useState<KeyboardMode>('letters');
  const [activeKeyId, setActiveKeyId] = useState<string | null>(null);

  const lastShiftTapRef = useRef<number>(0);
  const longPressTimerRef = useRef<NodeJS.Timeout | null>(null);
  const backspaceIntervalRef = useRef<NodeJS.Timeout | null>(null);

  const triggerHaptic = useCallback(() => {
    if (!hapticEnabled) return;
    if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
      try {
        navigator.vibrate(12);
      } catch {
        // Ignore vibration errors
      }
    }
  }, [hapticEnabled]);

  // Handle Shift toggle / Caps Lock
  const handleShiftClick = () => {
    triggerHaptic();
    if (soundEnabled) playKeySound('standard');

    const now = Date.now();
    const isDoubleTap = now - lastShiftTapRef.current < 350;
    lastShiftTapRef.current = now;

    if (shiftState === 'caps_lock') {
      setShiftState('off');
    } else if (isDoubleTap) {
      setShiftState('caps_lock');
    } else if (shiftState === 'shift') {
      setShiftState('off');
    } else {
      setShiftState('shift');
    }
  };

  const handleCharClick = (char: string, isFromLongPress = false) => {
    triggerHaptic();
    if (soundEnabled) playKeySound('standard');
    onInsertText(char);

    // Auto-revert single shift after typing one letter, but preserve if caps_lock
    if (!isFromLongPress && shiftState === 'shift') {
      setShiftState('off');
    }
  };

  const handleBackspaceClick = () => {
    triggerHaptic();
    if (soundEnabled) playKeySound('backspace');
    onBackspace();
  };

  const handleSpaceClick = () => {
    triggerHaptic();
    if (soundEnabled) playKeySound('space');
    onInsertText(' ');
  };

  const handleEnterClick = () => {
    triggerHaptic();
    if (soundEnabled) playKeySound('enter');
    onEnter();
  };

  // Long press for secondary hint symbol
  const startLongPress = (hintValue?: string) => {
    if (!hintValue) return;
    longPressTimerRef.current = setTimeout(() => {
      triggerHaptic();
      if (soundEnabled) playKeySound('standard');
      onInsertText(hintValue);
      // Cancel subsequent click
      setActiveKeyId(null);
    }, 450);
  };

  const cancelLongPress = () => {
    if (longPressTimerRef.current) {
      clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }
  };

  // Continuous backspace on hold
  const startContinuousBackspace = () => {
    handleBackspaceClick();
    longPressTimerRef.current = setTimeout(() => {
      backspaceIntervalRef.current = setInterval(() => {
        triggerHaptic();
        if (soundEnabled) playKeySound('backspace');
        onBackspace();
      }, 70);
    }, 400);
  };

  const stopContinuousBackspace = () => {
    cancelLongPress();
    if (backspaceIntervalRef.current) {
      clearInterval(backspaceIntervalRef.current);
      backspaceIntervalRef.current = null;
    }
  };

  // Clean up timers
  useEffect(() => {
    return () => {
      cancelLongPress();
      if (backspaceIntervalRef.current) {
        clearInterval(backspaceIntervalRef.current);
      }
    };
  }, []);

  const isUppercase = shiftState !== 'off';

  const [showClipboardTools, setShowClipboardTools] = useState<boolean>(false);

  return (
    <div
      id="xboard-keyboard-container"
      className="w-full bg-[#111216] select-none text-white pb-3 pt-1 px-1.5 flex flex-col justify-end shadow-2xl transition-colors duration-150 rounded-t-2xl border-t border-[#232630]"
      style={{ touchAction: 'manipulation' }}
    >
      {/* Top Suggestion & Clipboard Bar matching exact screenshot */}
      <div
        id="xboard-suggestion-strip"
        className="w-full h-11 flex items-center justify-between px-3 border-b border-[#1c1e26] mb-1.5 overflow-hidden"
      >
        {/* Left Close/Clear icon */}
        <button
          id="btn-clear-suggestion"
          type="button"
          onClick={() => {
            triggerHaptic();
            if (soundEnabled) playKeySound('standard');
            onClear();
          }}
          className="text-[#686f80] hover:text-[#a5adbf] active:scale-90 transition-transform p-1.5 rounded-full flex items-center justify-center cursor-pointer"
          title="Clear all text"
          aria-label="Clear all text"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2.5" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>

        {/* Clipboard Toolbar Mode or Word Suggestions */}
        {showClipboardTools ? (
          <div className="flex-1 flex items-center justify-around px-1 gap-1 text-xs animate-fadeIn">
            <button
              id="btn-action-select-all"
              type="button"
              onClick={() => {
                triggerHaptic();
                onSelectAll?.();
              }}
              disabled={!hasText}
              className="px-2.5 py-1 rounded bg-[#1C1F28] hover:bg-[#252936] text-white disabled:opacity-35 font-medium cursor-pointer active:scale-95"
            >
              Select All
            </button>
            <button
              id="btn-action-cut"
              type="button"
              onClick={() => {
                triggerHaptic();
                onCut?.();
              }}
              disabled={!hasSelection}
              className="px-2.5 py-1 rounded bg-[#1C1F28] hover:bg-[#252936] text-[#00DF6C] disabled:opacity-35 font-medium cursor-pointer active:scale-95"
            >
              Cut
            </button>
            <button
              id="btn-action-copy"
              type="button"
              onClick={() => {
                triggerHaptic();
                onCopy?.();
              }}
              disabled={!hasSelection && !hasText}
              className="px-2.5 py-1 rounded bg-[#1C1F28] hover:bg-[#252936] text-[#00DF6C] disabled:opacity-35 font-medium cursor-pointer active:scale-95"
            >
              Copy
            </button>
            <button
              id="btn-action-paste"
              type="button"
              onClick={() => {
                triggerHaptic();
                onPaste?.();
              }}
              className="px-2.5 py-1 rounded bg-[#1C1F28] hover:bg-[#252936] text-[#00DF6C] font-medium cursor-pointer active:scale-95"
            >
              Paste
            </button>
          </div>
        ) : (
          /* Suggestion words in Emerald Green */
          <div className="flex-1 flex items-center justify-around px-2">
            {suggestions.map((word, idx) => (
              <button
                key={`${word}-${idx}`}
                id={`btn-suggest-${idx}`}
                type="button"
                onClick={() => {
                  triggerHaptic();
                  if (soundEnabled) playKeySound('standard');
                  onSelectSuggestion(word);
                }}
                className="text-[#00DF6C] hover:text-[#38ef8d] active:scale-95 font-semibold text-base sm:text-lg tracking-wide px-3 py-1 rounded-md hover:bg-[#1a2e22]/40 transition cursor-pointer truncate max-w-[32%]"
              >
                {word}
              </button>
            ))}
          </div>
        )}

        {/* Right Toggle Clipboard / Edit Tools button */}
        <button
          id="btn-toggle-clipboard-tools"
          type="button"
          onClick={() => {
            triggerHaptic();
            setShowClipboardTools((prev) => !prev);
          }}
          className={`p-1.5 rounded-md transition-colors cursor-pointer ${
            showClipboardTools
              ? 'bg-[#00DF6C]/20 text-[#00DF6C]'
              : 'text-[#686f80] hover:text-[#a5adbf]'
          }`}
          title={showClipboardTools ? 'Show Suggestions' : 'Clipboard Tools (Select All, Cut, Copy, Paste)'}
          aria-label="Toggle Clipboard Tools"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
        </button>
      </div>

      {/* Emoji Drawer View */}
      {mode === 'emoji' ? (
        <div id="xboard-emoji-panel" className="w-full h-[230px] flex flex-col">
          <div className="flex-1 overflow-y-auto grid grid-cols-8 gap-1.5 p-2 scrollbar-thin scrollbar-thumb-gray-700">
            {POPULAR_EMOJIS.map((emoji, idx) => (
              <button
                key={idx}
                type="button"
                onClick={() => {
                  triggerHaptic();
                  if (soundEnabled) playKeySound('standard');
                  onInsertText(emoji);
                }}
                className="h-10 text-2xl flex items-center justify-center rounded-lg hover:bg-[#20232c] active:scale-90 transition-transform cursor-pointer"
              >
                {emoji}
              </button>
            ))}
          </div>

          {/* Emoji bottom navigation bar */}
          <div className="h-12 flex items-center justify-between px-2 pt-1 border-t border-[#1c1e26]">
            <button
              type="button"
              onClick={() => {
                triggerHaptic();
                setMode('letters');
              }}
              className="px-4 py-2 bg-[#20232b] text-white font-bold rounded-lg text-sm active:scale-95 cursor-pointer"
            >
              ABC
            </button>
            <span className="text-xs text-[#6e7485] font-medium">Popular Emojis</span>
            <button
              type="button"
              onClick={handleBackspaceClick}
              className="w-12 h-10 bg-[#20232b] rounded-lg flex items-center justify-center active:scale-95 cursor-pointer text-white"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.5-6.5a2 2 0 011.4-.5H20a2 2 0 012 2v10a2 2 0 01-2 2h-9.1a2 2 0 01-1.4-.5L3 12z" />
              </svg>
            </button>
          </div>
        </div>
      ) : (
        /* Keyboard Rows */
        <div className="w-full flex flex-col gap-1.5">
          {mode === 'letters' && (
            <>
              {/* Row 1: Q W E R T Y U I O P */}
              <div className="flex w-full gap-1 justify-between">
                {LETTER_ROWS[0].map((k) => {
                  const displayChar = isUppercase ? (k.shiftedValue || k.label) : (k.value || k.label.toLowerCase());
                  return (
                    <button
                      key={k.id}
                      id={`key-${k.id}`}
                      type="button"
                      onMouseDown={() => startLongPress(k.hintValue)}
                      onMouseUp={cancelLongPress}
                      onMouseLeave={cancelLongPress}
                      onTouchStart={() => startLongPress(k.hintValue)}
                      onTouchEnd={cancelLongPress}
                      onClick={() => handleCharClick(displayChar)}
                      className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex flex-col items-center justify-between py-1 border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition-all cursor-pointer relative"
                    >
                      <span className="text-[10px] text-[#6b7284] font-medium leading-none select-none">
                        {k.hint}
                      </span>
                      <span className="text-[19px] sm:text-[21px] font-medium text-white select-none leading-none pb-0.5">
                        {displayChar}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Row 2: A S D F G H J K L */}
              <div className="flex w-full gap-1 justify-center px-[4%]">
                {LETTER_ROWS[1].map((k) => {
                  const displayChar = isUppercase ? (k.shiftedValue || k.label) : (k.value || k.label.toLowerCase());
                  return (
                    <button
                      key={k.id}
                      id={`key-${k.id}`}
                      type="button"
                      onMouseDown={() => startLongPress(k.hintValue)}
                      onMouseUp={cancelLongPress}
                      onMouseLeave={cancelLongPress}
                      onTouchStart={() => startLongPress(k.hintValue)}
                      onTouchEnd={cancelLongPress}
                      onClick={() => handleCharClick(displayChar)}
                      className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex flex-col items-center justify-between py-1 border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition-all cursor-pointer relative"
                    >
                      <span className="text-[10px] text-[#6b7284] font-medium leading-none select-none">
                        {k.hint}
                      </span>
                      <span className="text-[19px] sm:text-[21px] font-medium text-white select-none leading-none pb-0.5">
                        {displayChar}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Row 3: Shift, Z X C V B N M, Backspace */}
              <div className="flex w-full gap-1 justify-between">
                {/* Neon Green Shift Key */}
                <button
                  id="key-shift"
                  type="button"
                  onClick={handleShiftClick}
                  className={`w-[14%] sm:w-[15%] h-[52px] sm:h-[56px] rounded-[8px] flex items-center justify-center transition-all cursor-pointer shadow-[0_2px_4px_rgba(0,0,0,0.4)] active:scale-[0.95] ${
                    shiftState !== 'off'
                      ? 'bg-[#00DF6C] text-[#052413] hover:bg-[#1fe67c]'
                      : 'bg-[#1E2027] text-[#9ba2b5] border border-[#272a34]/60 hover:bg-[#272a33]'
                  }`}
                  title={shiftState === 'caps_lock' ? 'Caps Lock Active' : shiftState === 'shift' ? 'Shift Active' : 'Shift'}
                  aria-label="Shift Key"
                >
                  {shiftState === 'caps_lock' ? (
                    // Caps lock icon with filled arrow & bar
                    <div className="flex flex-col items-center">
                      <svg className="w-5 h-5 fill-current" viewBox="0 0 24 24">
                        <path d="M12 3l7 7h-4v6H9v-6H5l7-7z" />
                      </svg>
                      <div className="w-3.5 h-[2px] bg-current rounded-full mt-0.5" />
                    </div>
                  ) : shiftState === 'shift' ? (
                    // Shift active: Hollow arrow with line below exactly matching screenshot
                    <div className="flex flex-col items-center">
                      <svg className="w-5 h-5 stroke-current fill-none stroke-[2.2]" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M5 11l7-7 7 7M12 4v11" />
                      </svg>
                      <div className="w-4 h-[2px] bg-current rounded-full mt-0.5" />
                    </div>
                  ) : (
                    // Shift off: clean outline arrow
                    <svg className="w-5 h-5 stroke-current fill-none stroke-[2]" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" d="M5 12l7-7 7 7M12 5v12" />
                    </svg>
                  )}
                </button>

                {/* Z X C V B N M */}
                {LETTER_ROWS[2].slice(1, -1).map((k) => {
                  const displayChar = isUppercase ? (k.shiftedValue || k.label) : (k.value || k.label.toLowerCase());
                  return (
                    <button
                      key={k.id}
                      id={`key-${k.id}`}
                      type="button"
                      onMouseDown={() => startLongPress(k.hintValue)}
                      onMouseUp={cancelLongPress}
                      onMouseLeave={cancelLongPress}
                      onTouchStart={() => startLongPress(k.hintValue)}
                      onTouchEnd={cancelLongPress}
                      onClick={() => handleCharClick(displayChar)}
                      className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex flex-col items-center justify-between py-1 border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition-all cursor-pointer relative"
                    >
                      <span className="text-[10px] text-[#6b7284] font-medium leading-none select-none">
                        {k.hint}
                      </span>
                      <span className="text-[19px] sm:text-[21px] font-medium text-white select-none leading-none pb-0.5">
                        {displayChar}
                      </span>
                    </button>
                  );
                })}

                {/* Backspace Key matching screenshot */}
                <button
                  id="key-backspace"
                  type="button"
                  onMouseDown={startContinuousBackspace}
                  onMouseUp={stopContinuousBackspace}
                  onMouseLeave={stopContinuousBackspace}
                  onTouchStart={startContinuousBackspace}
                  onTouchEnd={stopContinuousBackspace}
                  className="w-[14%] sm:w-[15%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition-all cursor-pointer text-[#a3a9ba]"
                  title="Backspace"
                  aria-label="Backspace"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="1.8" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.5-6.5a2 2 0 011.4-.5H20a2 2 0 012 2v10a2 2 0 01-2 2h-9.1a2 2 0 01-1.4-.5L3 12z" />
                  </svg>
                </button>
              </div>
            </>
          )}

          {/* Symbol Keyboard Rows */}
          {(mode === 'symbols' || mode === 'more_symbols') && (
            <>
              {/* Symbols Row 1 */}
              <div className="flex w-full gap-1 justify-between">
                {(mode === 'symbols' ? SYMBOL_ROWS[0] : MORE_SYMBOL_ROWS[0]).map((k) => (
                  <button
                    key={k.id}
                    id={`key-${k.id}`}
                    type="button"
                    onClick={() => handleCharClick(k.value || k.label)}
                    className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] text-[20px] font-medium text-white transition cursor-pointer"
                  >
                    {k.label}
                  </button>
                ))}
              </div>

              {/* Symbols Row 2 */}
              <div className="flex w-full gap-1 justify-center">
                {(mode === 'symbols' ? SYMBOL_ROWS[1] : MORE_SYMBOL_ROWS[1]).map((k) => (
                  <button
                    key={k.id}
                    id={`key-${k.id}`}
                    type="button"
                    onClick={() => handleCharClick(k.value || k.label)}
                    className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] text-[20px] font-medium text-white transition cursor-pointer"
                  >
                    {k.label}
                  </button>
                ))}
              </div>

              {/* Symbols Row 3 */}
              <div className="flex w-full gap-1 justify-between">
                <button
                  id="key-mode-toggle-symbols"
                  type="button"
                  onClick={() => {
                    triggerHaptic();
                    if (soundEnabled) playKeySound('standard');
                    setMode(mode === 'symbols' ? 'more_symbols' : 'symbols');
                  }}
                  className="w-[14%] sm:w-[15%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 text-[14px] font-bold text-[#b5bac7] transition cursor-pointer"
                >
                  {mode === 'symbols' ? '=\\<' : '?123'}
                </button>

                {(mode === 'symbols' ? SYMBOL_ROWS[2] : MORE_SYMBOL_ROWS[2]).slice(1, -1).map((k) => (
                  <button
                    key={k.id}
                    id={`key-${k.id}`}
                    type="button"
                    onClick={() => handleCharClick(k.value || k.label)}
                    className="flex-1 h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.96] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] text-[20px] font-medium text-white transition cursor-pointer"
                  >
                    {k.label}
                  </button>
                ))}

                <button
                  id="key-backspace-sym"
                  type="button"
                  onMouseDown={startContinuousBackspace}
                  onMouseUp={stopContinuousBackspace}
                  onMouseLeave={stopContinuousBackspace}
                  onTouchStart={startContinuousBackspace}
                  onTouchEnd={stopContinuousBackspace}
                  className="w-[14%] sm:w-[15%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition-all cursor-pointer text-[#a3a9ba]"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="1.8" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.5-6.5a2 2 0 011.4-.5H20a2 2 0 012 2v10a2 2 0 01-2 2h-9.1a2 2 0 01-1.4-.5L3 12z" />
                  </svg>
                </button>
              </div>
            </>
          )}

          {/* Row 4: 123 | Emoji | Comma | Space "X BOARD" | Period | Enter */}
          <div className="flex w-full gap-1 justify-between items-center mt-0.5">
            {/* 123 Switch Button */}
            <button
              id="key-mode-switch"
              type="button"
              onClick={() => {
                triggerHaptic();
                if (soundEnabled) playKeySound('standard');
                setMode(mode === 'letters' ? 'symbols' : 'letters');
              }}
              className="w-[12%] sm:w-[11%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] text-[15px] sm:text-[16px] font-medium text-white transition cursor-pointer"
            >
              {mode === 'letters' ? '123' : 'ABC'}
            </button>

            {/* Emoji Button */}
            <button
              id="key-emoji"
              type="button"
              onClick={() => {
                triggerHaptic();
                if (soundEnabled) playKeySound('standard');
                setMode(mode === 'emoji' ? 'letters' : 'emoji');
              }}
              className="w-[11%] sm:w-[10%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex items-center justify-center border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] text-[20px] transition cursor-pointer"
              title="Emoji"
              aria-label="Emoji"
            >
              😊
            </button>

            {/* Comma Key with subtle mic icon above */}
            <button
              id="key-comma"
              type="button"
              onClick={() => handleCharClick(',')}
              className="w-[10%] sm:w-[9%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex flex-col items-center justify-between py-1 border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition cursor-pointer"
            >
              <svg className="w-3 h-3 text-[#62697a]" fill="currentColor" viewBox="0 0 24 24">
                <path d="M12 14c1.66 0 3-1.34 3-3V5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3z" />
                <path d="M17 11c0 2.76-2.24 5-5 5s-5-2.24-5-5H5c0 3.53 2.61 6.43 6 6.92V21h2v-3.08c3.39-.49 6-3.39 6-6.92h-2z" />
              </svg>
              <span className="text-[18px] text-white font-semibold leading-none pb-1">,</span>
            </button>

            {/* Vibrant Neon Green Space Bar: "X BOARD" */}
            <button
              id="key-space"
              type="button"
              onClick={handleSpaceClick}
              className="flex-1 h-[52px] sm:h-[56px] bg-[#00DF6C] hover:bg-[#1fe67c] active:bg-[#00c760] active:scale-[0.98] rounded-[8px] flex items-center justify-center shadow-[0_2px_5px_rgba(0,223,108,0.3)] transition-all cursor-pointer mx-0.5"
              aria-label="Spacebar X BOARD"
            >
              <span className="text-[#052413] font-bold tracking-wider text-[15px] sm:text-[17px] select-none">
                X BOARD
              </span>
            </button>

            {/* Period Key with subtle !? hint above */}
            <button
              id="key-period"
              type="button"
              onClick={() => handleCharClick('.')}
              className="w-[10%] sm:w-[9%] h-[52px] sm:h-[56px] bg-[#1E2027] hover:bg-[#272a33] active:bg-[#343845] active:scale-[0.95] rounded-[8px] flex flex-col items-center justify-between py-1 border border-[#272a34]/60 shadow-[0_2px_3px_rgba(0,0,0,0.4)] transition cursor-pointer"
            >
              <span className="text-[9px] text-[#62697a] font-bold leading-none select-none">!?</span>
              <span className="text-[18px] text-white font-semibold leading-none pb-1">.</span>
            </button>

            {/* Neon Green Enter Key with spark/star and return arrow */}
            <button
              id="key-enter"
              type="button"
              onClick={handleEnterClick}
              className="w-[13%] sm:w-[12%] h-[52px] sm:h-[56px] bg-[#00DF6C] hover:bg-[#1fe67c] active:bg-[#00c760] active:scale-[0.95] rounded-[8px] flex items-center justify-center shadow-[0_2px_5px_rgba(0,223,108,0.3)] transition-all cursor-pointer relative"
              title="Enter / Return"
              aria-label="Enter"
            >
              {/* Subtle top-right or top-left sparkle icon matching image */}
              <div className="absolute top-1 left-2 opacity-50">
                <svg className="w-3 h-3 fill-[#052413]" viewBox="0 0 24 24">
                  <path d="M12 2l2.4 7.2L22 12l-7.6 2.8L12 22l-2.4-7.2L2 12l7.6-2.8z" />
                </svg>
              </div>
              <svg className="w-6 h-6 stroke-[#052413] fill-none stroke-[2.8]" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" d="M19 8v6a2 2 0 01-2 2H5m0 0l4-4m-4 4l4 4" />
              </svg>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
