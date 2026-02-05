/* ===============================================
   GALAXY PORTFOLIO MANAGEMENT - MAIN APPLICATION
   =============================================== */

// ================= CONFIGURATION =================
const CONFIG = {
    BASE_URL: "http://localhost:8080/portfolio",
    ALPHA_KEY: "demo",
    FINNHUB_KEY: "d61fl21r01quq5pg3t00d61fl21r01quq5pg3t0g",
    IPO_ALERTS_KEY: "768e0b79502ba98201d4a2ffb31a3f6c5eee4d26a98075b084dae585a37802a0",
    IPO_CACHE_TTL: 60 * 60 * 1000, // 1 hour in milliseconds
    AUTO_REFRESH_INTERVAL: 120000 // 2 minutes
};

// ================= GLOBAL VARIABLES =================
let allocationChart, performanceChart, debounceTimer;
let sellStockId, sellStockSymbol, sellStockBuyPrice;
let pricePerShare = 0;
let autoRefreshInterval;
let currentIPOStatus = 'upcoming';

// ================= DOM ELEMENTS =================
const addStockModal = document.getElementById('addStockModal');
const sellModal = document.getElementById('sellModal');
const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');
const symbolInput = document.getElementById('symbol');
const companyNameInput = document.getElementById('companyName');
const quantityInput = document.getElementById('quantity');
const buyPriceInput = document.getElementById('buyPrice');
const sellInfo = document.getElementById('sellInfo');
const sellQuantity = document.getElementById('sellQuantity');

// ================= REALIZED & UNREALIZED P/L TRACKING =================
function getRealizedProfits() {
    const stored = localStorage.getItem('realizedProfits');
    return stored ? JSON.parse(stored) : {};
}

function saveRealizedProfit(symbol, profit) {
    const profits = getRealizedProfits();
    profits[symbol] = (profits[symbol] || 0) + profit;
    localStorage.setItem('realizedProfits', JSON.stringify(profits));
}

function getTotalRealizedProfit() {
    const profits = getRealizedProfits();
    return Object.values(profits).reduce((sum, val) => sum + val, 0);
}

function calculateUnrealizedPL(stock, currentPrice) {
    return (currentPrice - stock.buyPrice) * stock.quantity;
}

function calculateRealizedPL(stock, sellPrice, soldQuantity) {
    return (sellPrice - stock.buyPrice) * soldQuantity;
}

// ================= NAVIGATION =================
function showSection(section) {
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    event.target.closest('.nav-item').classList.add('active');
    toast(`Switched to ${section}`);
}

function removeAllAssets() {
    if (confirm('Are you sure you want to remove all assets?')) {
        toast('Feature coming soon!');
    }
}

// ================= UI HELPERS =================
function toast(msg) {
    const toastEl = document.getElementById('toast');
    toastEl.innerText = msg;
    toastEl.style.display = "block";
    setTimeout(() => toastEl.style.display = "none", 2500);
}

function openAddStockModal() {
    addStockModal.classList.add('show');
    searchInput.value = '';
    symbolInput.value = '';
    companyNameInput.value = '';
    quantityInput.value = '';
    buyPriceInput.value = '';
    searchResults.innerHTML = '';
}

function closeAddStockModal() {
    addStockModal.classList.remove('show');
}

function closeSellModal() {
    sellModal.classList.remove('show');
}

// ================= AUTO REFRESH =================
async function refreshPortfolio() {
    toast('🔄 Refreshing prices...');
    await loadPortfolio();
    toast('✅ Prices updated!');
}

function startAutoRefresh() {
    autoRefreshInterval = setInterval(() => {
        console.log('Auto-refreshing portfolio prices...');
        loadPortfolio();
    }, CONFIG.AUTO_REFRESH_INTERVAL);
}

function stopAutoRefresh() {
    if (autoRefreshInterval) {
        clearInterval(autoRefreshInterval);
    }
}

// ================= SEARCH =================
function debouncedSearch() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(searchStocks, 700);
}

async function searchStocks() {
    const q = searchInput.value.trim().toUpperCase();
    if (q.length < 1) {
        searchResults.innerHTML = "";
        return;
    }

    try {
        const res = await fetch(
            `https://finnhub.io/api/v1/search?q=${q}&token=${CONFIG.FINNHUB_KEY}`
        );

        if (!res.ok) throw new Error("Search failed");

        const data = await res.json();
        searchResults.innerHTML = "";

        if (data.result && data.result.length > 0) {
            data.result.slice(0, 8).forEach(m => {
                const symbol = m.symbol;
                const name = m.description;
                const type = m.type || '';

                if (symbol && name) {
                    searchResults.innerHTML += `
                        <div onclick="selectStock('${symbol}','${name.replace(/'/g, "\\'")}')">
                            <strong>${symbol}</strong> – ${name}
                            ${type ? `<br><small style="color:#94a3b8;">${type}</small>` : ''}
                        </div>`;
                }
            });

            if (searchResults.innerHTML === "") {
                searchResults.innerHTML = '<div style="color:#64748b; padding:12px;">No results found</div>';
            }
        } else {
            searchResults.innerHTML = '<div style="color:#64748b; padding:12px;">No results found</div>';
        }
    } catch (err) {
        console.error('Search error:', err);
        searchResults.innerHTML = '<div style="color:#ef4444; padding:12px;">Search failed. Please try again.</div>';
    }
}

// ================= STOCK SELECTION & PRICING =================
async function selectStock(sym, name) {
    symbolInput.value = sym;
    companyNameInput.value = name;
    searchResults.innerHTML = "";
    quantityInput.value = 1;

    try {
        const res = await fetch(
            `https://finnhub.io/api/v1/quote?symbol=${sym}&token=${CONFIG.FINNHUB_KEY}`
        );

        if (!res.ok) throw new Error("Price fetch failed");

        const data = await res.json();
        pricePerShare = parseFloat(data.c || data.pc || 0);

        if (pricePerShare === 0) {
            toast('⚠️ Unable to fetch price. Please enter manually.');
            buyPriceInput.removeAttribute('readonly');
            buyPriceInput.value = '';
            buyPriceInput.placeholder = 'Enter price manually';
        } else {
            updateTotalPrice();
            toast(`✓ ${sym} price: $${pricePerShare.toFixed(2)}`);
        }
    } catch (err) {
        console.error('Price fetch error:', err);
        toast('⚠️ Unable to fetch price. Please enter manually.');
        pricePerShare = 0;
        buyPriceInput.removeAttribute('readonly');
        buyPriceInput.value = '';
        buyPriceInput.placeholder = 'Enter price manually';
    }
}

function updateTotalPrice() {
    const qty = parseInt(quantityInput.value || 0);
    if (qty > 0 && pricePerShare > 0) {
        buyPriceInput.value = `$${(pricePerShare * qty).toFixed(2)}`;
        buyPriceInput.setAttribute('readonly', 'readonly');
    } else {
        buyPriceInput.value = "";
    }
}

// ================= BACKEND OPERATIONS =================
async function addStock() {
    const sym = symbolInput.value.trim();
    const name = companyNameInput.value.trim();
    const qty = parseInt(quantityInput.value);

    if (!sym || !name || !qty || qty <= 0) {
        toast('⚠️ Please fill all fields');
        return;
    }

    let totalPrice = 0;
    if (pricePerShare > 0) {
        totalPrice = pricePerShare;
    } else {
        const manualPrice = parseFloat(buyPriceInput.value.replace(/[^0-9.]/g, ''));
        if (!manualPrice || manualPrice <= 0) {
            toast('⚠️ Please enter a valid price');
            return;
        }
        totalPrice = manualPrice / qty;
    }

    closeAddStockModal();

    try {
        const res = await fetch(`${CONFIG.BASE_URL}/stock`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                symbol: sym,
                companyName: name,
                quantity: qty,
                buyPrice: totalPrice.toFixed(2)
            })
        });

        if (!res.ok) throw new Error('Failed to add stock');

        toast("✓ Stock added successfully 🚀");
        loadPortfolio();

        symbolInput.value = '';
        companyNameInput.value = '';
        quantityInput.value = '';
        buyPriceInput.value = '';
        pricePerShare = 0;

    } catch (err) {
        console.error('Add stock error:', err);
        toast('⚠️ Failed to add stock');
    }
}

function openSellModal(id, sym, qty) {
    sellStockId = id;
    sellStockSymbol = sym;

    fetch(CONFIG.BASE_URL)
        .then(res => res.json())
        .then(data => {
            const stock = data.find(s => s.id === id);
            if (stock) {
                sellStockBuyPrice = stock.buyPrice;
            }
        });

    sellInfo.innerText = `${sym} | Available: ${qty}`;
    sellModal.classList.add('show');
}

async function confirmSell() {
    const soldQuantity = parseInt(sellQuantity.value);

    const currentPrice = await fetchCurrentPrice(sellStockSymbol);
    const sellPrice = currentPrice || sellStockBuyPrice;

    const realizedProfit = calculateRealizedPL(
        { buyPrice: sellStockBuyPrice },
        sellPrice,
        soldQuantity
    );
    saveRealizedProfit(sellStockSymbol, realizedProfit);

    closeSellModal();

    await fetch(`${CONFIG.BASE_URL}/stock/${sellStockId}/sell?quantity=${soldQuantity}`, {
        method: "PUT"
    });

    toast(`Stock sold 📉 | Realized P/L: ${realizedProfit >= 0 ? '+' : ''}$${Math.abs(realizedProfit).toFixed(2)}`);
    loadPortfolio();
}

// ================= FETCH CURRENT PRICE =================
async function fetchCurrentPrice(symbol) {
    try {
        const res = await fetch(
            `https://finnhub.io/api/v1/quote?symbol=${symbol}&token=${CONFIG.FINNHUB_KEY}`
        );

        if (!res.ok) throw new Error("Failed to fetch price");

        const data = await res.json();

        if (data.c && data.c > 0) {
            return data.c;
        }

        return null;
    } catch (err) {
        console.error(`Error fetching price for ${symbol}:`, err);
        return null;
    }
}
