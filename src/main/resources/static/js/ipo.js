/* ===============================================
   GALAXY - IPO ALERTS
   =============================================== */

// ================= CACHE HELPERS =================
function getIPOCache(status) {
    try {
        const cacheKey = `ipo_${status}`;
        const cached = localStorage.getItem(cacheKey);
        if (!cached) return null;

        const { data, timestamp } = JSON.parse(cached);
        const now = Date.now();

        if (now - timestamp < CONFIG.IPO_CACHE_TTL) {
            console.log(`Using cached IPO data for status: ${status}`);
            return data;
        } else {
            localStorage.removeItem(cacheKey);
            return null;
        }
    } catch (err) {
        console.error('Cache read error:', err);
        return null;
    }
}

function setIPOCache(status, data) {
    try {
        const cacheKey = `ipo_${status}`;
        const cacheData = {
            data: data,
            timestamp: Date.now()
        };
        localStorage.setItem(cacheKey, JSON.stringify(cacheData));
        console.log(`Cached IPO data for status: ${status}`);
    } catch (err) {
        console.error('Cache write error:', err);
    }
}

// ================= RENDER IPO CARDS =================
function renderIPOCards(data, status, fromCache = false) {
    const iposEl = document.getElementById('ipos');

    if (data.ipos && data.ipos.length) {
        iposEl.innerHTML = data.ipos.map(ipo => `
            <div class="ipo-card">
                <div class="ipo-header">
                    <img src="${ipo.logo || ''}" alt="${ipo.name}" class="ipo-logo"
                         onerror="this.style.display='none'">
                    <div class="ipo-name">
                        <h4>${ipo.name}</h4>
                        <span>${ipo.symbol} • ${ipo.type || 'IPO'}</span>
                    </div>
                </div>

                <div class="ipo-details">
                    <div class="ipo-detail">
                        <div class="ipo-detail-label">Issue Size</div>
                        <div class="ipo-detail-value">${ipo.issueSize || 'N/A'}</div>
                    </div>
                    <div class="ipo-detail">
                        <div class="ipo-detail-label">Price Range</div>
                        <div class="ipo-detail-value">₹${ipo.priceRange || 'N/A'}</div>
                    </div>
                    <div class="ipo-detail">
                        <div class="ipo-detail-label">Min Amount</div>
                        <div class="ipo-detail-value">₹${ipo.minAmount?.toLocaleString('en-IN') || 'N/A'}</div>
                    </div>
                    <div class="ipo-detail">
                        <div class="ipo-detail-label">Issue Date</div>
                        <div class="ipo-detail-value">${formatDate(ipo.startDate)} - ${formatDate(ipo.endDate)}</div>
                    </div>
                    <div class="ipo-detail">
                        <div class="ipo-detail-label">Listing Date</div>
                        <div class="ipo-detail-value">${formatDate(ipo.listingDate)}</div>
                    </div>
                </div>

                ${ipo.about ? `
                    <div class="ipo-about">
                        <strong>About:</strong> ${truncateText(ipo.about, 300)}
                    </div>
                ` : ''}
            </div>
        `).join('');
    } else {
        iposEl.innerHTML = `No ${status} IPOs found`;
    }

    const cacheIndicator = document.getElementById('cacheIndicator');
    if (fromCache) {
        cacheIndicator.innerHTML = '📦 Loaded from cache (updates every hour)';
    } else {
        cacheIndicator.innerHTML = '✨ Fresh data loaded';
    }
}

// ================= LOAD IPOs =================
async function loadIPOsByStatus(status, forceRefresh = false, eventTarget = null) {
    currentIPOStatus = status;

    document.querySelectorAll('.ipo-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    if (eventTarget) {
        eventTarget.classList.add('active');
    } else {
        const tabToActivate = document.querySelector(`.ipo-tab[onclick*="${status}"]`);
        if (tabToActivate) tabToActivate.classList.add('active');
    }

    if (!forceRefresh) {
        const cachedData = getIPOCache(status);
        if (cachedData) {
            renderIPOCards(cachedData, status, true);
            return;
        }
    }

    const iposEl = document.getElementById('ipos');
    iposEl.innerHTML = 'Loading...';
    const cacheIndicator = document.getElementById('cacheIndicator');
    cacheIndicator.innerHTML = '';

    try {
        const res = await fetch(`${CONFIG.BASE_URL}/stock/ipos?status=${status}`);
        const data = await res.json();

        setIPOCache(status, data);
        renderIPOCards(data, status, false);
    } catch (err) {
        console.error("IPO load error:", err);
        iposEl.innerHTML = "Unable to load IPO data";
        cacheIndicator.innerHTML = '';
    }
}

async function refreshIPOs() {
    const activeTab = document.querySelector('.ipo-tab.active');
    if (activeTab) {
        const cacheKey = `ipo_${currentIPOStatus}`;
        localStorage.removeItem(cacheKey);

        await loadIPOsByStatus(currentIPOStatus, true);
        toast('IPO data refreshed!');
    }
}

async function loadIPOs() {
    await loadIPOsByStatus('upcoming');
}
