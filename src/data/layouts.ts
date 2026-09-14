import { KeyDef } from '../types';

export const LETTER_ROWS: KeyDef[][] = [
  // Row 1
  [
    { id: 'q', label: 'Q', shiftedLabel: 'q', hint: '1', action: 'char', value: 'q', shiftedValue: 'Q', hintValue: '1' },
    { id: 'w', label: 'W', shiftedLabel: 'w', hint: '2', action: 'char', value: 'w', shiftedValue: 'W', hintValue: '2' },
    { id: 'e', label: 'E', shiftedLabel: 'e', hint: '3', action: 'char', value: 'e', shiftedValue: 'E', hintValue: '3' },
    { id: 'r', label: 'R', shiftedLabel: 'r', hint: '4', action: 'char', value: 'r', shiftedValue: 'R', hintValue: '4' },
    { id: 't', label: 'T', shiftedLabel: 't', hint: '5', action: 'char', value: 't', shiftedValue: 'T', hintValue: '5' },
    { id: 'y', label: 'Y', shiftedLabel: 'y', hint: '6', action: 'char', value: 'y', shiftedValue: 'Y', hintValue: '6' },
    { id: 'u', label: 'U', shiftedLabel: 'u', hint: '7', action: 'char', value: 'u', shiftedValue: 'U', hintValue: '7' },
    { id: 'i', label: 'I', shiftedLabel: 'i', hint: '8', action: 'char', value: 'i', shiftedValue: 'I', hintValue: '8' },
    { id: 'o', label: 'O', shiftedLabel: 'o', hint: '9', action: 'char', value: 'o', shiftedValue: 'O', hintValue: '9' },
    { id: 'p', label: 'P', shiftedLabel: 'p', hint: '0', action: 'char', value: 'p', shiftedValue: 'P', hintValue: '0' },
  ],
  // Row 2
  [
    { id: 'a', label: 'A', shiftedLabel: 'a', hint: '@', action: 'char', value: 'a', shiftedValue: 'A', hintValue: '@' },
    { id: 's', label: 'S', shiftedLabel: 's', hint: '#', action: 'char', value: 's', shiftedValue: 'S', hintValue: '#' },
    { id: 'd', label: 'D', shiftedLabel: 'd', hint: '&', action: 'char', value: 'd', shiftedValue: 'D', hintValue: '&' },
    { id: 'f', label: 'F', shiftedLabel: 'f', hint: '*', action: 'char', value: 'f', shiftedValue: 'F', hintValue: '*' },
    { id: 'g', label: 'G', shiftedLabel: 'g', hint: '-', action: 'char', value: 'g', shiftedValue: 'G', hintValue: '-' },
    { id: 'h', label: 'H', shiftedLabel: 'h', hint: '+', action: 'char', value: 'h', shiftedValue: 'H', hintValue: '+' },
    { id: 'j', label: 'J', shiftedLabel: 'j', hint: '=', action: 'char', value: 'j', shiftedValue: 'J', hintValue: '=' },
    { id: 'k', label: 'K', shiftedLabel: 'k', hint: '(', action: 'char', value: 'k', shiftedValue: 'K', hintValue: '(' },
    { id: 'l', label: 'L', shiftedLabel: 'l', hint: ')', action: 'char', value: 'l', shiftedValue: 'L', hintValue: ')' },
  ],
  // Row 3
  [
    { id: 'shift', label: 'Shift', action: 'shift', width: 'shift', variant: 'accent' },
    { id: 'z', label: 'Z', shiftedLabel: 'z', hint: '_', action: 'char', value: 'z', shiftedValue: 'Z', hintValue: '_' },
    { id: 'x', label: 'X', shiftedLabel: 'x', hint: '"', action: 'char', value: 'x', shiftedValue: 'X', hintValue: '"' },
    { id: 'c', label: 'C', shiftedLabel: 'c', hint: "'", action: 'char', value: 'c', shiftedValue: 'C', hintValue: "'" },
    { id: 'v', label: 'V', shiftedLabel: 'v', hint: ':', action: 'char', value: 'v', shiftedValue: 'V', hintValue: ':' },
    { id: 'b', label: 'B', shiftedLabel: 'b', hint: ';', action: 'char', value: 'b', shiftedValue: 'B', hintValue: ';' },
    { id: 'n', label: 'N', shiftedLabel: 'n', hint: '/', action: 'char', value: 'n', shiftedValue: 'N', hintValue: '/' },
    { id: 'm', label: 'M', shiftedLabel: 'm', hint: '!', action: 'char', value: 'm', shiftedValue: 'M', hintValue: '!' },
    { id: 'backspace', label: '⌫', action: 'backspace', width: 'shift', variant: 'normal' },
  ],
  // Row 4
  [
    { id: 'mode_123', label: '123', action: 'mode', width: 'normal', variant: 'normal' },
    { id: 'emoji', label: '😊', action: 'emoji', width: 'normal', variant: 'normal' },
    { id: 'comma', label: ',', hint: '🎙', action: 'char', value: ',', hintValue: ',', width: 'normal' },
    { id: 'space', label: 'X BOARD', action: 'space', width: 'space', variant: 'accent' },
    { id: 'period', label: '.', hint: ',!?', action: 'char', value: '.', hintValue: '?', width: 'normal' },
    { id: 'enter', label: '↵', action: 'enter', width: 'normal', variant: 'accent' },
  ]
];

export const SYMBOL_ROWS: KeyDef[][] = [
  // Row 1
  [
    { id: 'sym_1', label: '1', action: 'char', value: '1' },
    { id: 'sym_2', label: '2', action: 'char', value: '2' },
    { id: 'sym_3', label: '3', action: 'char', value: '3' },
    { id: 'sym_4', label: '4', action: 'char', value: '4' },
    { id: 'sym_5', label: '5', action: 'char', value: '5' },
    { id: 'sym_6', label: '6', action: 'char', value: '6' },
    { id: 'sym_7', label: '7', action: 'char', value: '7' },
    { id: 'sym_8', label: '8', action: 'char', value: '8' },
    { id: 'sym_9', label: '9', action: 'char', value: '9' },
    { id: 'sym_0', label: '0', action: 'char', value: '0' },
  ],
  // Row 2
  [
    { id: 'sym_at', label: '@', action: 'char', value: '@' },
    { id: 'sym_hash', label: '#', action: 'char', value: '#' },
    { id: 'sym_dollar', label: '$', action: 'char', value: '$' },
    { id: 'sym_percent', label: '%', action: 'char', value: '%' },
    { id: 'sym_amp', label: '&', action: 'char', value: '&' },
    { id: 'sym_star', label: '*', action: 'char', value: '*' },
    { id: 'sym_dash', label: '-', action: 'char', value: '-' },
    { id: 'sym_plus', label: '+', action: 'char', value: '+' },
    { id: 'sym_paren_l', label: '(', action: 'char', value: '(' },
    { id: 'sym_paren_r', label: ')', action: 'char', value: ')' },
  ],
  // Row 3
  [
    { id: 'mode_more_sym', label: '=\\<', action: 'mode', width: 'shift', variant: 'normal' },
    { id: 'sym_excl', label: '!', action: 'char', value: '!' },
    { id: 'sym_quote_d', label: '"', action: 'char', value: '"' },
    { id: 'sym_quote_s', label: "'", action: 'char', value: "'" },
    { id: 'sym_colon', label: ':', action: 'char', value: ':' },
    { id: 'sym_semi', label: ';', action: 'char', value: ';' },
    { id: 'sym_slash', label: '/', action: 'char', value: '/' },
    { id: 'sym_quest', label: '?', action: 'char', value: '?' },
    { id: 'backspace', label: '⌫', action: 'backspace', width: 'shift', variant: 'normal' },
  ],
  // Row 4
  [
    { id: 'mode_abc', label: 'ABC', action: 'mode', width: 'normal', variant: 'normal' },
    { id: 'emoji', label: '😊', action: 'emoji', width: 'normal', variant: 'normal' },
    { id: 'comma', label: ',', hint: '🎙', action: 'char', value: ',', width: 'normal' },
    { id: 'space', label: 'X BOARD', action: 'space', width: 'space', variant: 'accent' },
    { id: 'period', label: '.', hint: ',!?', action: 'char', value: '.', width: 'normal' },
    { id: 'enter', label: '↵', action: 'enter', width: 'normal', variant: 'accent' },
  ]
];

export const MORE_SYMBOL_ROWS: KeyDef[][] = [
  // Row 1
  [
    { id: 'msym_tilde', label: '~', action: 'char', value: '~' },
    { id: 'msym_grave', label: '`', action: 'char', value: '`' },
    { id: 'msym_pipe', label: '|', action: 'char', value: '|' },
    { id: 'msym_bullet', label: '•', action: 'char', value: '•' },
    { id: 'msym_sqrt', label: '√', action: 'char', value: '√' },
    { id: 'msym_pi', label: 'π', action: 'char', value: 'π' },
    { id: 'msym_div', label: '÷', action: 'char', value: '÷' },
    { id: 'msym_times', label: '×', action: 'char', value: '×' },
    { id: 'msym_para', label: '¶', action: 'char', value: '¶' },
    { id: 'msym_delta', label: '∆', action: 'char', value: '∆' },
  ],
  // Row 2
  [
    { id: 'msym_pound', label: '£', action: 'char', value: '£' },
    { id: 'msym_euro', label: '€', action: 'char', value: '€' },
    { id: 'msym_yen', label: '¥', action: 'char', value: '¥' },
    { id: 'msym_cent', label: '¢', action: 'char', value: '¢' },
    { id: 'msym_caret', label: '^', action: 'char', value: '^' },
    { id: 'msym_degree', label: '°', action: 'char', value: '°' },
    { id: 'msym_equal', label: '=', action: 'char', value: '=' },
    { id: 'msym_bracket_l', label: '{', action: 'char', value: '{' },
    { id: 'msym_bracket_r', label: '}', action: 'char', value: '}' },
  ],
  // Row 3
  [
    { id: 'mode_123_back', label: '?123', action: 'mode', width: 'shift', variant: 'normal' },
    { id: 'msym_backslash', label: '\\', action: 'char', value: '\\' },
    { id: 'msym_copyright', label: '©', action: 'char', value: '©' },
    { id: 'msym_registered', label: '®', action: 'char', value: '®' },
    { id: 'msym_trademark', label: '™', action: 'char', value: '™' },
    { id: 'msym_lessthan', label: '<', action: 'char', value: '<' },
    { id: 'msym_greaterthan', label: '>', action: 'char', value: '>' },
    { id: 'msym_ellipsis', label: '…', action: 'char', value: '…' },
    { id: 'backspace', label: '⌫', action: 'backspace', width: 'shift', variant: 'normal' },
  ],
  // Row 4
  [
    { id: 'mode_abc', label: 'ABC', action: 'mode', width: 'normal', variant: 'normal' },
    { id: 'emoji', label: '😊', action: 'emoji', width: 'normal', variant: 'normal' },
    { id: 'comma', label: ',', hint: '🎙', action: 'char', value: ',', width: 'normal' },
    { id: 'space', label: 'X BOARD', action: 'space', width: 'space', variant: 'accent' },
    { id: 'period', label: '.', hint: ',!?', action: 'char', value: '.', width: 'normal' },
    { id: 'enter', label: '↵', action: 'enter', width: 'normal', variant: 'accent' },
  ]
];

export const POPULAR_EMOJIS = [
  '😀', '😃', '😄', '😁', '😆', '😅', '😂', '🤣', '😊', '😇',
  '🙂', '🙃', '😉', '😌', '😍', '🥰', '😘', '😗', '😙', '😚',
  '😋', '😛', '😝', '😜', '🤪', '🤨', '🧐', '🤓', '😎', '🤩',
  '🥳', '😏', '😒', '😞', '😔', '😟', '😕', '🙁', '☹️', '😣',
  '👍', '👎', '👌', '✌️', '🤞', '🤟', '🤘', '🤙', '👈', '👉',
  '👆', '👇', '☝️', '✋', '🤚', '🖐️', '🖖', '👋', '🤝', '👏',
  '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍', '🤎', '💔',
  '🔥', '✨', '⚡', '🎉', '🚀', '💯', '⭐', '🌟', '💥', '💡'
];
