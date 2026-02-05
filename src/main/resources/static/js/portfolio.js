/* ===============================================
   GALAXY - PORTFOLIO MANAGEMENT
   =============================================== */

// ================= LOAD PORTFOLIO =================
async function loadPortfolio() {
    try {
        const res = await fetch(CONFIG.BASE_URL);
        const data = await res.json();

        const tableBody = document.getElementById('holdingsTableBody');
        tableBody.innerHTML = "<tr><td colspan='8' style='text-align:center;color:#94a3b8;'>Loading prices...</td></tr>";

        const labels = [], values = [];
        let totalValue = 0;
        let totalCost = 0;
        let totalUnrealizedPL = 0;

        if (data && data.length > 0) {
            const pricePromises = data.map(stock => fetchCurrentPrice(stock.symbol));
            const currentPrices = await Promise.all(pricePromises);

            tableBody.innerHTML = "";

            for (let i = 0; i < data.length; i++) {
                const stock = data[i];

                const currentPrice = currentPrices[i] || stock.buyPrice;
                const costBasis = stock.buyPrice * stock.quantity;
                const marketValue = currentPrice * stock.quantity;
                const pl = marketValue - costBasis;
                const plPercent = ((pl / costBasis) * 100).toFixed(2);

                totalValue += marketValue;
                totalCost += costBasis;

                const unrealizedPL = calculateUnrealizedPL(stock, currentPrice);
                totalUnrealizedPL += unrealizedPL;

                labels.push(stock.symbol);
                values.push(marketValue);

                const priceIndicator = currentPrices[i] ? '🟢' : '🟡';
                const priceTitle = currentPrices[i] ? 'Real-time price' : 'Using buy price (API unavailable)';

                tableBody.innerHTML += `
                    <tr>
                        <td><strong>${stock.symbol}</strong></td>
                        <td>${stock.companyName || stock.name || '-'}</td>
                        <td>${stock.quantity}</td>
                        <td title="${priceTitle}">
                            <span style="font-size:10px; margin-right:4px;">${priceIndicator}</span>
                            $${currentPrice.toFixed(2)}
                        </td>
                        <td>$${costBasis.toFixed(2)}</td>
                        <td>$${marketValue.toFixed(2)}</td>
                        <td class="${pl >= 0 ? 'profit-positive' : 'profit-negative'}">
                            ${pl >= 0 ? '+' : ''}$${Math.abs(pl).toFixed(2)} (${pl >= 0 ? '+' : ''}${plPercent}%)
                        </td>
                        <td>
                            <button class="btn-danger" style="padding:6px 12px;font-size:12px"
                                onclick="openSellModal(${stock.id},'${stock.symbol}',${stock.quantity})">
                                Sell
                            </button>
                        </td>
                    </tr>`;
            }

            // Update header stats
            const totalReturn = totalValue - totalCost;
            const totalReturnPercent = ((totalReturn / totalCost) * 100).toFixed(1);

            document.getElementById('totalValue').textContent = `$${totalValue.toFixed(2)}`;
            document.getElementById('totalValue').className = `stat-value ${totalReturn >= 0 ? 'positive' : 'negative'}`;

            document.getElementById('totalReturn').textContent = `${totalReturn >= 0 ? '+' : ''}${totalReturnPercent}%`;
            document.getElementById('totalReturn').className = `stat-value ${totalReturn >= 0 ? 'positive' : 'negative'}`;

            document.getElementById('unrealizedPL').textContent = `${totalUnrealizedPL >= 0 ? '+' : ''}$${Math.abs(totalUnrealizedPL).toFixed(2)}`;
            document.getElementById('unrealizedPL').className = `stat-value ${totalUnrealizedPL >= 0 ? 'positive' : 'negative'}`;

            const totalRealizedPL = getTotalRealizedProfit();
            document.getElementById('realizedPL').textContent = `${totalRealizedPL >= 0 ? '+' : ''}$${Math.abs(totalRealizedPL).toFixed(2)}`;
            document.getElementById('realizedPL').className = `stat-value ${totalRealizedPL >= 0 ? 'positive' : 'negative'}`;

            const now = new Date();
            const timeStr = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
            document.getElementById('lastUpdated').innerHTML = `🟢 Live prices • Last updated: ${timeStr}`;

        } else {
            tableBody.innerHTML = '<tr><td colspan="8" style="text-align:center;color:#94a3b8;">No holdings yet. Add your first stock!</td></tr>';
            document.getElementById('lastUpdated').innerHTML = '⚪ No holdings';

            document.getElementById('unrealizedPL').textContent = '$0.00';
            document.getElementById('unrealizedPL').className = 'stat-value';

            const totalRealizedPL = getTotalRealizedProfit();
            document.getElementById('realizedPL').textContent = `${totalRealizedPL >= 0 ? '+' : ''}$${Math.abs(totalRealizedPL).toFixed(2)}`;
            document.getElementById('realizedPL').className = `stat-value ${totalRealizedPL >= 0 ? 'positive' : 'negative'}`;
        }

        // Update allocation chart
        if (allocationChart) allocationChart.destroy();

        const ctx1 = document.getElementById('allocationChart').getContext('2d');
        allocationChart = new Chart(ctx1, {
            type: 'doughnut',
            data: {
                labels: labels.length > 0 ? labels : ['No Data'],
                datasets: [{
                    data: values.length > 0 ? values : [1],
                    backgroundColor: [
                        '#2563eb', '#10b981', '#ef4444', '#f59e0b',
                        '#8b5cf6', '#ec4899', '#06b6d4', '#84cc16'
                    ],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } }
            }
        });

        // Create legend
        const legendContainer = document.getElementById('allocationLegend');
        legendContainer.innerHTML = labels.map((label, i) => `
            <div class="legend-item">
                <div class="legend-color" style="background:${allocationChart.data.datasets[0].backgroundColor[i]}"></div>
                <span>${label}</span>
            </div>
        `).join('');

        // Update performance chart
        if (performanceChart) performanceChart.destroy();

        const ctx2 = document.getElementById('performanceChart').getContext('2d');
        performanceChart = new Chart(ctx2, {
            type: 'line',
            data: {
                labels: ['Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Max'],
                datasets: [{
                    label: 'Portfolio Value',
                    data: [1200, 1250, 1220, 1280, 1320, 13500, 1400, 14500, 1430, 14800, 15000, totalValue || 152430],
                    borderColor: '#2563eb',
                    backgroundColor: 'rgba(37, 99, 235, 0.1)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: {
                        beginAtZero: false,
                        ticks: {
                            callback: function(value) {
                                return '$' + (value/1000) + 'k';
                            }
                        }
                    }
                }
            }
        });

    } catch (err) {
        console.error('Portfolio load error:', err);
        toast('Failed to load portfolio');
    }
}
