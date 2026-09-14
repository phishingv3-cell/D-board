export type ShiftState = 'off' | 'shift' | 'caps_lock';

export type KeyboardMode = 'letters' | 'symbols' | 'more_symbols' | 'emoji';

export interface KeyDef {
  id: string;
  label: string;
  shiftedLabel?: string;
  hint?: string;
  action: 'char' | 'shift' | 'backspace' | 'mode' | 'emoji' | 'space' | 'enter';
  value?: string;
  shiftedValue?: string;
  hintValue?: string;
  width?: 'normal' | 'shift' | 'space' | 'wide' | 'extra-wide';
  variant?: 'normal' | 'accent' | 'special';
  icon?: string;
}

export interface PredictionCandidate {
  word: string;
  score: number;
}

export interface AndroidFile {
  path: string;
  name: string;
  language: 'kotlin' | 'xml' | 'kts' | 'json';
  content: string;
  description: string;
}
