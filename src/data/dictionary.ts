// Common English frequency dictionary for autocompletion and prediction
export const COMMON_WORDS = [
  "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
  "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
  "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
  "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
  "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
  "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
  "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
  "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
  "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
  "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
  "keyboard", "android", "phone", "message", "typing", "mobile", "smart", "quick",
  "great", "today", "tomorrow", "tonight", "thanks", "thank", "please", "welcome",
  "happy", "awesome", "perfect", "better", "best", "always", "never", "sometimes",
  "really", "very", "much", "many", "little", "before", "right", "left", "here",
  "where", "why", "again", "hello", "hey", "friend", "family", "world", "love",
  "work", "project", "code", "app", "application", "screen", "button", "board",
  "let's", "let", "going", "doing", "having", "taking", "coming", "thinking", "learning"
];

// Contextual next-word predictions based on last word
export const NEXT_WORD_MAP: Record<string, string[]> = {
  "": ["I", "Let's", "The"],
  "i": ["am", "will", "have", "can", "want", "think", "need"],
  "you": ["are", "can", "have", "will", "want", "know"],
  "we": ["are", "can", "will", "have", "should", "need"],
  "let's": ["go", "do", "meet", "start", "see", "talk"],
  "the": ["best", "new", "first", "way", "time", "app"],
  "how": ["are", "is", "was", "do", "can"],
  "what": ["is", "are", "do", "about", "time"],
  "good": ["morning", "night", "luck", "job", "day"],
  "thank": ["you", "so", "very"],
  "thanks": ["for", "a", "again"],
  "have": ["a", "to", "been", "done", "fun"],
  "it": ["is", "was", "will", "looks", "works"],
  "this": ["is", "was", "app", "keyboard", "one"]
};

export function getSuggestions(input: string): string[] {
  if (!input || input.trim().length === 0) {
    return ["I", "Let's", "The"];
  }

  // Check the last typed word
  const words = input.trimEnd().split(/\s+/);
  const currentToken = input.endsWith(" ") ? "" : words[words.length - 1].toLowerCase();
  const previousToken = input.endsWith(" ")
    ? words[words.length - 1].toLowerCase()
    : words.length > 1
    ? words[words.length - 2].toLowerCase()
    : "";

  // If user just typed space and is starting a new word, give next word prediction
  if (currentToken === "") {
    if (previousToken && NEXT_WORD_MAP[previousToken]) {
      return NEXT_WORD_MAP[previousToken].slice(0, 3);
    }
    return ["the", "and", "to"];
  }

  // Match prefix
  const matches = COMMON_WORDS.filter(w => w.startsWith(currentToken) && w !== currentToken);
  
  if (matches.length === 0) {
    // Return capitalized or simple variation
    const capitalized = currentToken.charAt(0).toUpperCase() + currentToken.slice(1);
    return [capitalized, currentToken.toUpperCase(), currentToken + "s"];
  }

  // Preserve case style: if user typed capital, show capitalized suggestions
  const isCapitalized = words[words.length - 1].charAt(0) === words[words.length - 1].charAt(0).toUpperCase() &&
                        words[words.length - 1].charAt(0) !== words[words.length - 1].charAt(0).toLowerCase();

  const formattedMatches = matches.slice(0, 3).map(w => {
    if (isCapitalized) {
      return w.charAt(0).toUpperCase() + w.slice(1);
    }
    return w;
  });

  return formattedMatches;
}
