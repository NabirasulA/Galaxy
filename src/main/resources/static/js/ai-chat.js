/* ===============================================
   GALAXY - AI CHAT (Powered by Puter.js + Grok)
   =============================================== */

// ================= VARIABLES =================
let isAIChatOpen = false;
let conversationHistory = [];

// System prompt for Galaxy AI
const GALAXY_AI_SYSTEM_PROMPT = `You are Galaxy AI, an intelligent financial advisor assistant integrated into a portfolio management application called Galaxy.

Your role is to:
1. Provide helpful, accurate financial advice and insights
2. Analyze portfolio data when provided
3. Explain investment concepts in simple terms
4. Help users understand market trends and stock performance
5. Suggest portfolio optimization strategies
6. Answer questions about stocks, ETFs, mutual funds, and other investments

Guidelines:
- Be concise but informative
- Use bullet points for clarity when listing multiple items
- Include relevant emojis to make responses engaging (📈 📉 💰 🎯 ⚠️ 💡)
- Always remind users that this is not personalized financial advice and they should consult a professional
- If you don't know something, say so honestly
- When analyzing portfolios, consider diversification, risk, and potential returns

Format your responses in a clear, readable way.`;

// ================= TOGGLE CHAT =================
function toggleAIChat() {
    const container = document.getElementById('aiChatContainer');
    isAIChatOpen = !isAIChatOpen;

    if (isAIChatOpen) {
        container.classList.add('show');
        document.getElementById('aiChatInput').focus();
    } else {
        container.classList.remove('show');
    }
}

// ================= ADD MESSAGE TO CHAT =================
function addMessageToChat(message, role) {
    const messagesContainer = document.getElementById('aiChatMessages');
    const messageDiv = document.createElement('div');
    messageDiv.className = `ai-message ${role}`;

    // Format the message (convert markdown-like formatting)
    let formattedMessage = message
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\n/g, '<br>')
        .replace(/```([\s\S]*?)```/g, '<pre>$1</pre>');

    messageDiv.innerHTML = formattedMessage;
    messagesContainer.appendChild(messageDiv);

    // Scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// ================= TYPING INDICATOR =================
function showTypingIndicator() {
    const messagesContainer = document.getElementById('aiChatMessages');
    const typingDiv = document.createElement('div');
    typingDiv.className = 'ai-message assistant typing';
    typingDiv.id = 'typingIndicator';
    typingDiv.innerHTML = '<span></span><span></span><span></span>';
    messagesContainer.appendChild(typingDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function removeTypingIndicator() {
    const typingIndicator = document.getElementById('typingIndicator');
    if (typingIndicator) {
        typingIndicator.remove();
    }
}

// ================= BUILD PORTFOLIO CONTEXT =================
async function buildPortfolioContext() {
    try {
        const res = await fetch(CONFIG.BASE_URL);
        const stocks = await res.json();

        if (!stocks || stocks.length === 0) {
            return "User has no stocks in their portfolio yet.";
        }

        let context = "Current Portfolio Holdings:\n";
        let totalInvested = 0;

        for (const stock of stocks) {
            const invested = stock.totalInvestedAmount || (stock.buyPrice * stock.quantity);
            totalInvested += invested;
            context += `- ${stock.symbol} (${stock.companyName || 'N/A'}): ${stock.quantity} shares @ $${stock.buyPrice.toFixed(2)} avg cost (Total: $${invested.toFixed(2)})\n`;
        }

        context += `\nTotal Portfolio Investment: $${totalInvested.toFixed(2)}`;
        context += `\nNumber of Holdings: ${stocks.length}`;

        return context;
    } catch (error) {
        console.error('Error building portfolio context:', error);
        return "Unable to fetch portfolio data.";
    }
}

// ================= SEND AI MESSAGE =================
async function sendAIMessage() {
    const input = document.getElementById('aiChatInput');
    const sendBtn = document.getElementById('aiSendBtn');
    const message = input.value.trim();

    if (!message) return;

    // Add user message to chat
    addMessageToChat(message, 'user');
    input.value = '';

    // Disable input while processing
    input.disabled = true;
    sendBtn.disabled = true;

    // Show typing indicator
    showTypingIndicator();

    try {
        // Build portfolio context
        const portfolioContext = await buildPortfolioContext();

        // Build the full prompt with context
        const fullPrompt = `${GALAXY_AI_SYSTEM_PROMPT}

Current Portfolio Context:
${portfolioContext}

User Question: ${message}`;

        // Call Grok via Puter.js - using exact model name from docs
        const response = await puter.ai.chat(fullPrompt, {
            model: 'x-ai/grok-4.1-fast'
        });

        removeTypingIndicator();

        if (response && response.message && response.message.content) {
            addMessageToChat(response.message.content, 'assistant');
        } else if (response && typeof response === 'string') {
            addMessageToChat(response, 'assistant');
        } else {
            addMessageToChat('I received a response but couldn\'t parse it. Please try again.', 'assistant');
            console.log('Unexpected response format:', response);
        }

    } catch (error) {
        removeTypingIndicator();
        console.error('AI Chat error:', error);

        // Provide helpful error message
        let errorMsg = '❌ Sorry, I encountered an error. ';
        if (error && error.message) {
            errorMsg += error.message;
        } else if (error && typeof error === 'string') {
            errorMsg += error;
        } else {
            errorMsg += 'Please try again in a moment.';
        }
        addMessageToChat(errorMsg, 'assistant');
    }

    // Re-enable input
    input.disabled = false;
    sendBtn.disabled = false;
    input.focus();
}

// ================= QUICK MESSAGE =================
function sendQuickMessage(message) {
    document.getElementById('aiChatInput').value = message;
    sendAIMessage();
}

// ================= CLEAR CHAT HISTORY =================
function clearAIChatHistory() {
    const messagesContainer = document.getElementById('aiChatMessages');
    messagesContainer.innerHTML = `
        <div class="ai-message assistant">
            👋 Hi! I'm <strong>Galaxy AI</strong>, your intelligent portfolio assistant powered by <strong>Grok</strong>. I can help you with:
            <br><br>
            📊 Portfolio analysis & insights<br>
            📈 Stock recommendations<br>
            💡 Investment advice<br>
            🎓 Financial education
            <br><br>
            How can I help you today?
        </div>
    `;
}

// ================= KEYBOARD SHORTCUTS =================
document.addEventListener('keydown', (e) => {
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault();
        toggleAIChat();
    }
    if (e.key === 'Escape' && isAIChatOpen) {
        toggleAIChat();
    }
});
