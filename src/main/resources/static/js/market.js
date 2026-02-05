/* ===============================================
   GALAXY - MARKET DATA & NEWS
   =============================================== */

// ================= MARKET DATA =================
function renderMarketItem(item, type) {
    const isGainer = type === 'gainer';
    const isLoser = type === 'loser';
    const changeClass = isGainer ? 'profit-positive' : (isLoser ? 'profit-negative' : '');

    return `
        <div class="market-item">
            <div class="market-ticker">${item.ticker}</div>
            <div style="display:grid; grid-template-columns: repeat(2, 1fr); gap:8px; margin-top:8px;">
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase; margin-bottom:2px;">Price</div>
                    <div style="font-weight:600; color:#1e293b;">$${parseFloat(item.price).toFixed(2)}</div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase; margin-bottom:2px;">Change</div>
                    <div class="${changeClass}" style="font-weight:600;">
                        ${item.change_amount > 0 ? '+' : ''}${parseFloat(item.change_amount).toFixed(2)}
                    </div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase; margin-bottom:2px;">Change %</div>
                    <div class="${changeClass}" style="font-weight:600;">
                        ${item.change_percentage}
                    </div>
                </div>
                <div>
                    <div style="font-size:11px; color:#64748b; text-transform:uppercase; margin-bottom:2px;">Volume</div>
                    <div style="font-weight:600; color:#1e293b;">${formatVolume(item.volume)}</div>
                </div>
            </div>
        </div>
    `;
}

function formatVolume(volume) {
    const num = parseInt(volume);
    if (num >= 1000000000) {
        return (num / 1000000000).toFixed(2) + 'B';
    } else if (num >= 1000000) {
        return (num / 1000000).toFixed(2) + 'M';
    } else if (num >= 1000) {
        return (num / 1000).toFixed(2) + 'K';
    }
    return num.toLocaleString();
}

async function loadMarket() {
    try {
        const fetchAndParse = async (endpoint) => {
            try {
                const response = await fetch(`${CONFIG.BASE_URL}${endpoint}`);
                if (!response.ok) {
                    console.warn(`${endpoint} returned status ${response.status}`);
                    return [];
                }
                const text = await response.text();
                if (!text || text.trim() === '') {
                    console.warn(`${endpoint} returned empty response`);
                    return [];
                }
                return JSON.parse(text);
            } catch (err) {
                console.warn(`Failed to fetch ${endpoint}:`, err.message);
                return [];
            }
        };

        const g = await fetchAndParse('/stock/gainers');
        const l = await fetchAndParse('/stock/losers');

        const gainersEl = document.getElementById('gainers');
        const losersEl = document.getElementById('losers');

        gainersEl.innerHTML = (g && g.length)
            ? g.slice(0,5).map(x => renderMarketItem(x, 'gainer')).join('')
            : '<div style="color:#64748b; text-align:center; padding:20px;">No strong gainers today</div>';

        losersEl.innerHTML = (l && l.length)
            ? l.slice(0,5).map(x => renderMarketItem(x, 'loser')).join('')
            : '<div style="color:#64748b; text-align:center; padding:20px;">No strong losers today</div>';
    } catch (err) {
        console.error("Market data error:", err);
        const gainersEl = document.getElementById('gainers');
        const losersEl = document.getElementById('losers');
        gainersEl.innerHTML = losersEl.innerHTML = '<div style="color:#ef4444; text-align:center; padding:20px;">Unable to load data</div>';
    }
}

// ================= NEWS =================
async function loadNews() {
    try {
        const res = await fetch(
            `https://finnhub.io/api/v1/news?category=general&token=${CONFIG.FINNHUB_KEY}`
        );

        if (!res.ok) throw new Error("News API failed");

        const newsData = await res.json();
        const newsEl = document.getElementById('news');

        console.log('Finnhub News data:', newsData);

        if (Array.isArray(newsData) && newsData.length > 0) {
            newsEl.innerHTML = newsData.slice(0, 5)
                .map(n => {
                    const date = new Date(n.datetime * 1000);
                    const timeAgo = getTimeAgo(date);

                    return `
                    <div class="market-item">
                        <a href="${n.url}" target="_blank" style="text-decoration:none; color:inherit;">
                            <div style="display:flex; gap:12px; align-items:start;">
                                ${n.image ? `<img src="${n.image}" alt="" style="width:60px; height:60px; object-fit:cover; border-radius:8px; flex-shrink:0;">` : ''}
                                <div style="flex:1; min-width:0;">
                                    <strong style="color:#1e293b; display:block; margin-bottom:4px; line-height:1.4; font-size:14px;">
                                        ${n.headline || 'No headline'}
                                    </strong>
                                    <small style="color:#64748b; display:block; margin-bottom:4px; font-size:12px;">
                                        ${n.source || 'Unknown'} • ${timeAgo}
                                    </small>
                                    ${n.summary ? `<p style="color:#64748b; font-size:12px; margin:0; line-height:1.4;">${truncateText(n.summary, 120)}</p>` : ''}
                                </div>
                            </div>
                        </a>
                    </div>`;
                })
                .join("");
        } else {
            newsEl.innerHTML = '<div style="color:#64748b; text-align:center; padding:20px;">No market news available</div>';
        }

    } catch (err) {
        console.error("News load error:", err);
        document.getElementById('news').innerHTML = '<div style="color:#ef4444; text-align:center; padding:20px;">Unable to load market news</div>';
    }
}

// ================= HELPER FUNCTIONS =================
function getTimeAgo(date) {
    const seconds = Math.floor((new Date() - date) / 1000);

    let interval = Math.floor(seconds / 31536000);
    if (interval > 1) return interval + " years ago";
    if (interval === 1) return "1 year ago";

    interval = Math.floor(seconds / 2592000);
    if (interval > 1) return interval + " months ago";
    if (interval === 1) return "1 month ago";

    interval = Math.floor(seconds / 86400);
    if (interval > 1) return interval + " days ago";
    if (interval === 1) return "1 day ago";

    interval = Math.floor(seconds / 3600);
    if (interval > 1) return interval + " hours ago";
    if (interval === 1) return "1 hour ago";

    interval = Math.floor(seconds / 60);
    if (interval > 1) return interval + " minutes ago";
    if (interval === 1) return "1 minute ago";

    return "just now";
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function formatDate(dateStr) {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}
