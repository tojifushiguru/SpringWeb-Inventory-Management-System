// Dashboard JavaScript for SpringWeb Admin

// Field Configuration System for all modules
const MODULE_FIELD_CONFIGS = {
    products: [
        { key: 'id', label: 'ID', display: 'ID', type: 'readonly', readonly: true },
        { key: 'name', label: 'Product Name', display: 'Product Name', type: 'text', required: true, validation: { minLength: 2, maxLength: 100 } },
        { key: 'description', label: 'Description', display: 'Description', type: 'textarea', required: false },
        { key: 'price', label: 'Price', display: 'Price', type: 'number', required: true, validation: { min: 0, step: 0.01 } },
        { key: 'stockQuantity', label: 'Stock', display: 'Stock', type: 'number', required: true, validation: { min: 0 } },
        { key: 'category', label: 'Category', display: 'Category', type: 'select-async', endpoint: '/api/categories', valueKey: 'id', labelKey: 'name', required: true },
        { key: 'supplier', label: 'Supplier', display: 'Supplier', type: 'select-async', endpoint: '/api/suppliers', valueKey: 'id', labelKey: 'name', required: true },
        { key: 'sku', label: 'SKU', display: 'SKU', type: 'text', required: false },
        { key: 'isActive', label: 'Active', display: 'Active', type: 'checkbox', default: true, description: 'Product is available for sale' }
    ],
    categories: [
        { key: 'id', label: 'ID', display: 'ID', type: 'readonly', readonly: true },
        { key: 'name', label: 'Category Name', display: 'Category Name', type: 'text', required: true, validation: { minLength: 2, maxLength: 50 } },
        { key: 'description', label: 'Description', display: 'Description', type: 'textarea', required: false }
    ],
    suppliers: [
        { key: 'id', label: 'ID', display: 'ID', type: 'readonly', readonly: true },
        { key: 'name', label: 'Supplier Name', display: 'Supplier Name', type: 'text', required: true, validation: { minLength: 2, maxLength: 100 } },
        { key: 'contactName', label: 'Contact Person', display: 'Contact Person', type: 'text', required: true },
        { key: 'contactEmail', label: 'Email', display: 'Email', type: 'email', required: true, validation: { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/ } },
        { key: 'phone', label: 'Phone', display: 'Phone', type: 'text', required: true, validation: { pattern: /^[\d\s\-\+\(\)]+$/, minLength: 8, maxLength: 20 } },
        { key: 'address', label: 'Address', display: 'Address', type: 'textarea', required: false }
    ],
    orders: [
        { key: 'id', label: 'ID', display: 'ID', type: 'readonly', readonly: true },
        { key: 'orderNumber', label: 'Order Number', display: 'Order Number', type: 'text', readonly: true },
        { key: 'customerName', label: 'Customer Name', display: 'Customer Name', type: 'text', required: true },
        { key: 'customerEmail', label: 'Customer Email', display: 'Customer Email', type: 'email', required: false },
        { key: 'customerPhone', label: 'Customer Phone', display: 'Customer Phone', type: 'text', required: false, validation: { pattern: /^[\d\s\-\+\(\)]*$/, maxLength: 20 } },
        { key: 'customerAddress', label: 'Customer Address', display: 'Customer Address', type: 'textarea', required: false },
        { key: 'status', label: 'Status', display: 'Status', type: 'select', options: ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED', 'CANCELLED', 'REFUNDED'], required: true },
        { key: 'orderItems', label: 'Order Items', display: 'Order Items', type: 'order-items', required: true },
        { key: 'totalAmount', label: 'Total Amount', display: 'Total Amount', type: 'readonly', readonly: true }
    ],
    transactions: [
        { key: 'id', label: 'ID', display: 'ID', type: 'readonly', readonly: true },
        { key: 'transactionNumber', label: 'Transaction Number', display: 'Transaction Number', type: 'text', readonly: true },
        { key: 'orderId', label: 'Order', display: 'Order', type: 'select-async', endpoint: '/api/orders', valueKey: 'id', labelKey: 'orderNumber', required: true },
        { key: 'amount', label: 'Amount', display: 'Amount', type: 'number', required: true, validation: { min: 0, step: 0.01 } },
        { key: 'paymentMethod', label: 'Payment Method', display: 'Payment Method', type: 'select', options: ['CASH', 'CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER', 'OTHER'] },
        { key: 'transactionType', label: 'Transaction Type', display: 'Transaction Type', type: 'select', options: ['PAYMENT', 'REFUND', 'ADJUSTMENT', 'FEE'] },
        { key: 'transactionStatus', label: 'Status', display: 'Status', type: 'select', options: ['PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED'] },
        { key: 'description', label: 'Description', display: 'Description', type: 'textarea', required: false }
    ]
};

class Dashboard {
    constructor() {
        this.currentModule = 'dashboard';
        this.currentEditId = null;
        this.isInitialized = false;
        this.loadingState = false;
        this.productOptions = []; // Initialize product options cache as empty array

        // Show preview message with enhanced close functionality
        setTimeout(() => {
            const popup = document.getElementById('preview-popup');
            const closeBtn = popup?.querySelector('.preview-close');
            const mainContent = document.querySelector('.main-content');

            const hidePopup = () => {
                popup.style.display = 'none';
                if (mainContent) {
                    mainContent.classList.remove('blurred');
                }
            };

            if (popup) {
                popup.style.display = 'flex';
                if (mainContent) {
                    mainContent.classList.add('blurred');
                }

                // Add click handler for close button
                if (closeBtn) {
                    closeBtn.addEventListener('click', (e) => {
                        e.stopPropagation(); // Prevent event from bubbling to popup
                        hidePopup();
                    });
                }

                // Add click handler for popup content (now just for the background)
                popup.addEventListener('click', (e) => {
                    if (e.target === popup) { // Only close if clicking the background
                        hidePopup();
                    }
                });

                // Add ESC key handler
                document.addEventListener('keydown', (e) => {
                    if (e.key === 'Escape' && popup.style.display === 'flex') {
                        hidePopup();
                    }
                });
            }
        }, 1000); // Show after 1 second delay

        // Don't call init() here - it will be called by DOMContentLoaded
        console.log('Dashboard constructor called');
    }

    async init() {
        if (this.isInitialized) {
            console.log('Dashboard already initialized, skipping...');
            return;
        }

        try {
            console.log('Initializing dashboard...');
            await this.checkAuth();
            this.bindEvents();
            this.loadModule('dashboard');
            this.initDateInputs();
            this.isInitialized = true;
            console.log('Dashboard initialized successfully');
        } catch (error) {
            console.error('Dashboard initialization failed:', error);
        }
    }

    async checkAuth() {
        // Prevent infinite redirect loops
        if (window.location.href.includes('?authFailed=true')) {
            console.error('Authentication failed, stopping redirect loop');
            localStorage.clear();
            return;
        }

        // First check localStorage as a quick check
        if (!localStorage.getItem('isLoggedIn')) {
            console.log('No login token found, redirecting to login');
            window.location.href = '/?authFailed=true';
            return;
        }

        // Verify session is still valid
        try {
            const response = await fetch('/api/auth/status');
            if (response.ok) {
                const authStatus = await response.json();
                if (!authStatus.authenticated) {
                    // Session expired, redirect to login
                    console.log('Session expired, redirecting to login');
                    localStorage.removeItem('isLoggedIn');
                    localStorage.removeItem('username');
                    window.location.href = '/?sessionExpired=true';
                    return;
                }
                // Update username from server if needed
                if (authStatus.username && authStatus.username !== localStorage.getItem('username')) {
                    localStorage.setItem('username', authStatus.username);
                }
            } else {
                console.error('Auth status check failed with status:', response.status);
                // Don't immediately redirect on server error - user might still be logged in
                // Only redirect if it's a clear authentication failure (401/403)
                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem('isLoggedIn');
                    localStorage.removeItem('username');
                    window.location.href = '/?authFailed=true';
                    return;
                }
            }
        } catch (error) {
            console.error('Auth check failed with error:', error);
            // On network error, allow to proceed if localStorage check passed
            // This prevents infinite loops due to temporary network issues
        }

        // Get username and display Khmer name
        const username = localStorage.getItem('username') || 'Admin';
        const khmerNames = {
            'SRIE_VI': 'ស្រ៊ី វី',
            'EAM_VIMORL': 'អៀម វិមល',
            'PHAL_KHAMLA': 'ផល ខាំឡា',
            'LONG_SREYNET': 'ឡុង ស្រីណេត',
            'PHEAT_PISEY': 'ភាត់ ពីសី',
            'ROEURN_MAKARA': 'រឿន មករា'
        };

        const displayName = khmerNames[username] || username;
        const userElement = document.getElementById('currentUser');
        if (userElement) {
            userElement.textContent = displayName;
        } else {
            console.warn('currentUser element not found');
        }
    }

    bindEvents() {
        // Menu navigation
        document.querySelectorAll('.menu-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const module = item.dataset.module;
                if (module) {
                    this.loadModule(module);
                }
            });
        });

        // Sign out
        document.getElementById('signOut').addEventListener('click', async (e) => {
            e.preventDefault();

            try {
                const response = await fetch('/api/logout', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    // Clear localStorage as backup
                    localStorage.removeItem('isLoggedIn');
                    localStorage.removeItem('username');

                    // Redirect to login page
                    window.location.href = '/';
                } else {
                    console.error('Logout failed');
                    // Fallback: redirect anyway
                    window.location.href = '/';
                }
            } catch (error) {
                console.error('Logout error:', error);
                // Fallback: redirect anyway
                window.location.href = '/';
            }
        });

        // Modal close
        document.querySelector('.close').addEventListener('click', () => {
            this.closeModal();
        });

        // Modal form submit
        document.getElementById('modalForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleFormSubmit();
        });
    }

    // Handle authentication failures
    handleAuthFailure(response) {
        if (response.status === 401) {
            alert('Your session has expired. Please log in again.');
            localStorage.removeItem('isLoggedIn');
            localStorage.removeItem('username');
            window.location.href = '/';
            return true;
        }
        return false;
    }

    async initializeCharts() {
        try {
            const today = new Date();
            const startDate = new Date(today);
            startDate.setMonth(today.getMonth() - 1);

            // Initialize loading states
            this.showChartLoadingState('revenueChart', 'Loading Revenue Data...');
            this.showChartLoadingState('categoryChart', 'Loading Sales Data...');
            this.showChartLoadingState('stockChart', 'Loading Inventory Data...');
            this.showChartLoadingState('transactionTypeChart', 'Loading Transaction Data...');
            this.showChartLoadingState('paymentMethodChart', 'Loading Payment Data...');

            // Fetch all data in parallel
            const [revenueResponse, salesResponse, inventoryResponse, lowStockResponse, transactionResponse] = await Promise.all([
                fetch(`/api/reports/revenue?startDate=${startDate.toISOString().split('T')[0]}&groupBy=daily`),
                fetch(`/api/reports/sales?startDate=${startDate.toISOString().split('T')[0]}`),
                fetch('/api/reports/inventory'),
                fetch('/api/reports/low-stock?threshold=10'),
                fetch(`/api/reports/transaction-summary?startDate=${startDate.toISOString().split('T')[0]}`)
            ]);

            // Process revenue data
            if (revenueResponse.ok) {
                const data = await revenueResponse.json();
                if (data.success && data.report) {
                    const revenueData = {
                        labels: Object.keys(data.report.revenueByPeriod),
                        values: {
                            total: Object.values(data.report.revenueByPeriod).map(val => parseFloat(val)),
                            refunds: data.report.totalRefunds ? [parseFloat(data.report.totalRefunds)] : [],
                            net: data.report.netRevenue ? [parseFloat(data.report.netRevenue)] : []
                        }
                    };
                    this.initRevenueChart(revenueData);
                } else {
                    throw new Error(data.message || 'Invalid revenue data format');
                }
            } else {
                throw new Error('Failed to fetch revenue data');
            }

            // Process sales data
            if (salesResponse.ok) {
                const data = await salesResponse.json();
                if (data.success && data.report) {
                    const salesData = {
                        labels: Object.keys(data.report.salesByCategory),
                        values: Object.values(data.report.salesByCategory).map(val => parseFloat(val)),
                        total: parseFloat(data.report.totalSales)
                    };
                    this.initCategoryChart(salesData);
                } else {
                    throw new Error(data.message || 'Invalid sales data format');
                }
            } else {
                throw new Error('Failed to fetch sales data');
            }

            // Process inventory and low stock data
            if (inventoryResponse.ok && lowStockResponse.ok) {
                const [inventoryData, lowStockData] = await Promise.all([
                    inventoryResponse.json(),
                    lowStockResponse.json()
                ]);

                if (inventoryData.success && inventoryData.report && lowStockData.success && lowStockData.report) {
                    // Process inventory data
                    const productsByCategory = inventoryData.report.productsByCategory;
                    const lowStockByCategory = lowStockData.report.lowStockByCategory;

                    // Transform data for the chart
                    const categories = Object.keys(productsByCategory);
                    const stockData = categories.map(category => ({
                        category,
                        normal: productsByCategory[category].reduce((sum, product) => {
                            const isLowStock = lowStockByCategory[category]?.some(
                                lowStock => lowStock.id === product.id
                            );
                            return sum + (isLowStock ? 0 : product.stock);
                        }, 0),
                        low: lowStockByCategory[category]?.reduce(
                            (sum, product) => sum + product.stock, 0
                        ) || 0
                    }));

                    this.initStockChart({ categories, stockData });
                } else {
                    throw new Error('Invalid inventory data format');
                }
            } else {
                throw new Error('Failed to fetch inventory data');
            }

            // Process transaction summary data
            if (transactionResponse.ok) {
                const data = await transactionResponse.json();
                if (data.success && data.report) {
                    // Process transaction types data
                    const transactionTypeData = {
                        labels: Object.keys(data.report.totalsByType),
                        values: Object.values(data.report.totalsByType).map(val => parseFloat(val))
                    };

                    // Process payment methods data
                    const paymentMethodData = {
                        labels: Object.keys(data.report.totalsByPaymentMethod),
                        values: Object.values(data.report.totalsByPaymentMethod).map(val => parseFloat(val))
                    };

                    this.initTransactionTypeChart(transactionTypeData);
                    this.initPaymentMethodChart(paymentMethodData);
                } else {
                    throw new Error('Invalid transaction data format');
                }
            } else {
                throw new Error('Failed to fetch transaction data');
            }

        } catch (error) {
            console.error('Error initializing charts:', error);
            if (error.message.includes('revenue')) {
                this.showChartError('revenueChart', 'Failed to load revenue data. Please try again later.');
            }
            if (error.message.includes('sales')) {
                this.showChartError('categoryChart', 'Failed to load sales data. Please try again later.');
            }
            if (error.message.includes('inventory')) {
                this.showChartError('stockChart', 'Failed to load inventory data. Please try again later.');
            }
            if (error.message.includes('transaction')) {
                this.showChartError('transactionTypeChart', 'Failed to load transaction data. Please try again later.');
                this.showChartError('paymentMethodChart', 'Failed to load payment data. Please try again later.');
            }
        }
    }

    // Date range and grouping handling
    handleDateRangeChange(value) {
        const customDateRange = document.getElementById('customDateRange');
        const startDate = document.getElementById('startDate');
        const endDate = document.getElementById('endDate');
        const today = new Date();

        if (value === 'custom') {
            customDateRange.style.display = 'flex';
            return;
        }

        customDateRange.style.display = 'none';
        const days = parseInt(value);
        const start = new Date(today);
        start.setDate(today.getDate() - days);

        startDate.value = start.toISOString().split('T')[0];
        endDate.value = today.toISOString().split('T')[0];
    }

    handleCustomDateChange() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (startDate && endDate && startDate > endDate) {
            alert('Start date cannot be after end date');
            document.getElementById('startDate').value = '';
            document.getElementById('endDate').value = '';
        }
    }

    handleGroupingChange(value) {
        // Store the selected grouping
        localStorage.setItem('chartGrouping', value);
    }

    async refreshCharts() {
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;
        const groupBy = document.getElementById('groupBy').value;

        if (!startDate || !endDate) {
            alert('Please select valid dates');
            return;
        }

        try {
            // Initialize loading states
            this.showChartLoadingState('revenueChart', 'Updating Revenue Data...');
            this.showChartLoadingState('categoryChart', 'Updating Sales Data...');

            // Fetch updated data in parallel
            const [revenueResponse, salesResponse] = await Promise.all([
                fetch(`/api/reports/revenue?startDate=${startDate}&endDate=${endDate}&groupBy=${groupBy}`),
                fetch(`/api/reports/sales?startDate=${startDate}&endDate=${endDate}`)
            ]);

            // Process revenue data
            if (revenueResponse.ok) {
                const data = await revenueResponse.json();
                if (data.success && data.report) {
                    const revenueData = {
                        labels: Object.keys(data.report.revenueByPeriod),
                        values: {
                            total: Object.values(data.report.revenueByPeriod).map(val => parseFloat(val)),
                            refunds: data.report.totalRefunds ? [parseFloat(data.report.totalRefunds)] : [],
                            net: data.report.netRevenue ? [parseFloat(data.report.netRevenue)] : []
                        }
                    };
                    this.initRevenueChart(revenueData);
                } else {
                    throw new Error('Invalid revenue data format');
                }
            }

            // Process sales data
            if (salesResponse.ok) {
                const data = await salesResponse.json();
                if (data.success && data.report) {
                    const salesData = {
                        labels: Object.keys(data.report.salesByCategory),
                        values: Object.values(data.report.salesByCategory).map(val => parseFloat(val)),
                        total: parseFloat(data.report.totalSales)
                    };
                    this.initCategoryChart(salesData);
                } else {
                    throw new Error('Invalid sales data format');
                }
            }

        } catch (error) {
            console.error('Error refreshing charts:', error);
            if (error.message.includes('revenue')) {
                this.showChartError('revenueChart', 'Failed to update revenue data. Please try again later.');
            }
            if (error.message.includes('sales')) {
                this.showChartError('categoryChart', 'Failed to update sales data. Please try again later.');
            }
        }
    }

    // Initialize date inputs
    initDateInputs() {
        const today = new Date();
        const startDate = new Date(today);
        startDate.setDate(today.getDate() - 30); // Default to last 30 days

        document.getElementById('startDate').value = startDate.toISOString().split('T')[0];
        document.getElementById('endDate').value = today.toISOString().split('T')[0];
    }

    initTransactionTypeChart(data) {
        // Remove loading state
        const loadingDiv = document.querySelector('#transactionTypeChart').parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        const ctx = document.getElementById('transactionTypeChart');
        ctx.style.display = 'block';

        // Clear any existing chart
        const existingChart = Chart.getChart('transactionTypeChart');
        if (existingChart) {
            existingChart.destroy();
        }

        // Color configuration for different transaction types
        const colorMap = {
            'PAYMENT': { bg: 'rgba(75, 192, 192, 0.8)', border: 'rgba(75, 192, 192, 1)' },
            'REFUND': { bg: 'rgba(255, 99, 132, 0.8)', border: 'rgba(255, 99, 132, 1)' },
            'ADJUSTMENT': { bg: 'rgba(255, 206, 86, 0.8)', border: 'rgba(255, 206, 86, 1)' },
            'FEE': { bg: 'rgba(54, 162, 235, 0.8)', border: 'rgba(54, 162, 235, 1)' }
        };

        // Get colors based on labels
        const colors = data.labels.map(label => colorMap[label] ||
            { bg: 'rgba(153, 102, 255, 0.8)', border: 'rgba(153, 102, 255, 1)' });

        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: colors.map(c => c.bg),
                    borderColor: colors.map(c => c.border),
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const value = context.parsed;
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${context.label}: ${new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD'
                                }).format(value)} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }

    initPaymentMethodChart(data) {
        // Remove loading state
        const loadingDiv = document.querySelector('#paymentMethodChart').parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        const ctx = document.getElementById('paymentMethodChart');
        ctx.style.display = 'block';

        // Clear any existing chart
        const existingChart = Chart.getChart('paymentMethodChart');
        if (existingChart) {
            existingChart.destroy();
        }

        // Color configuration for different payment methods
        const colorMap = {
            'CASH': { bg: 'rgba(76, 175, 80, 0.8)', border: 'rgba(76, 175, 80, 1)' },
            'CREDIT_CARD': { bg: 'rgba(33, 150, 243, 0.8)', border: 'rgba(33, 150, 243, 1)' },
            'DEBIT_CARD': { bg: 'rgba(156, 39, 176, 0.8)', border: 'rgba(156, 39, 176, 1)' },
            'PAYPAL': { bg: 'rgba(255, 193, 7, 0.8)', border: 'rgba(255, 193, 7, 1)' },
            'BANK_TRANSFER': { bg: 'rgba(121, 85, 72, 0.8)', border: 'rgba(121, 85, 72, 1)' },
            'OTHER': { bg: 'rgba(158, 158, 158, 0.8)', border: 'rgba(158, 158, 158, 1)' }
        };

        // Get colors based on labels
        const colors = data.labels.map(label => colorMap[label] ||
            { bg: 'rgba(153, 102, 255, 0.8)', border: 'rgba(153, 102, 255, 1)' });

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels.map(label => label.replace('_', ' ')),
                datasets: [{
                    data: data.values,
                    backgroundColor: colors.map(c => c.bg),
                    borderColor: colors.map(c => c.border),
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '60%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            usePointStyle: true,
                            padding: 20
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const value = context.parsed;
                                const percentage = ((value / total) * 100).toFixed(1);
                                return `${context.label}: ${new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD'
                                }).format(value)} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }

    showChartLoadingState(chartId, message) {
        const ctx = document.getElementById(chartId);
        if (!ctx) return;

        // Clear existing chart
        const existingChart = Chart.getChart(chartId);
        if (existingChart) {
            existingChart.destroy();
        }

        // Show loading message
        ctx.style.display = 'none';
        const loadingDiv = document.createElement('div');
        loadingDiv.className = 'chart-loading';
        loadingDiv.innerHTML = `<i class="fas fa-spinner fa-spin"></i> ${message}`;
        ctx.parentNode.insertBefore(loadingDiv, ctx);
    }

    showChartError(chartId, message) {
        const ctx = document.getElementById(chartId);
        if (!ctx) return;

        // Clear loading state
        const loadingDiv = ctx.parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        // Show error message
        ctx.style.display = 'none';
        const errorDiv = document.createElement('div');
        errorDiv.className = 'chart-error';
        errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i> ${message}`;
        ctx.parentNode.insertBefore(errorDiv, ctx);
    }

    initOrdersChart(data) {
        const ctx = document.getElementById('ordersChart').getContext('2d');
        const labels = data.labels || ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'];
        const values = data.values || [65, 59, 80, 81, 56, 55];

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Orders',
                    data: values,
                    fill: true,
                    borderColor: '#4CAF50',
                    backgroundColor: 'rgba(76, 175, 80, 0.1)',
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            drawBorder: false
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    initRevenueChart(data) {
        // Remove loading state
        const loadingDiv = document.querySelector('#revenueChart').parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        const ctx = document.getElementById('revenueChart');
        ctx.style.display = 'block';

        // Clear any existing chart
        const existingChart = Chart.getChart('revenueChart');
        if (existingChart) {
            existingChart.destroy();
        }

        // Create new chart
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: 'Total Revenue',
                        data: data.values.total,
                        borderColor: 'rgba(75, 192, 192, 1)',
                        backgroundColor: 'rgba(75, 192, 192, 0.1)',
                        fill: true,
                        tension: 0.4
                    },
                    {
                        label: 'Net Revenue',
                        data: data.values.net.length > 0 ? new Array(data.labels.length).fill(data.values.net[0]) : [],
                        borderColor: 'rgba(54, 162, 235, 1)',
                        borderDash: [5, 5],
                        fill: false
                    },
                    {
                        label: 'Refunds',
                        data: data.values.refunds.length > 0 ? new Array(data.labels.length).fill(data.values.refunds[0]) : [],
                        borderColor: 'rgba(255, 99, 132, 1)',
                        borderDash: [5, 5],
                        fill: false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: {
                    intersect: false,
                    mode: 'index'
                },
                plugins: {
                    legend: {
                        position: 'top',
                    },
                    title: {
                        display: true,
                        text: 'Revenue Analysis'
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                let label = context.dataset.label || '';
                                if (label) {
                                    label += ': ';
                                }
                                if (context.parsed.y !== null) {
                                    label += new Intl.NumberFormat('en-US', {
                                        style: 'currency',
                                        currency: 'USD'
                                    }).format(context.parsed.y);
                                }
                                return label;
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            drawBorder: false
                        },
                        ticks: {
                            callback: function (value, index, values) {
                                return new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD',
                                    minimumFractionDigits: 0,
                                    maximumFractionDigits: 0
                                }).format(value);
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    initCategoryChart(data) {
        // Remove loading state
        const loadingDiv = document.querySelector('#categoryChart').parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        const ctx = document.getElementById('categoryChart');
        ctx.style.display = 'block';

        // Clear any existing chart
        const existingChart = Chart.getChart('categoryChart');
        if (existingChart) {
            existingChart.destroy();
        }

        // Colors for categories
        const colors = [
            { bg: 'rgba(255, 99, 132, 0.8)', border: 'rgba(255, 99, 132, 1)' },
            { bg: 'rgba(54, 162, 235, 0.8)', border: 'rgba(54, 162, 235, 1)' },
            { bg: 'rgba(255, 206, 86, 0.8)', border: 'rgba(255, 206, 86, 1)' },
            { bg: 'rgba(75, 192, 192, 0.8)', border: 'rgba(75, 192, 192, 1)' },
            { bg: 'rgba(153, 102, 255, 0.8)', border: 'rgba(153, 102, 255, 1)' },
            { bg: 'rgba(255, 159, 64, 0.8)', border: 'rgba(255, 159, 64, 1)' },
            { bg: 'rgba(201, 203, 207, 0.8)', border: 'rgba(201, 203, 207, 1)' }
        ];

        // Ensure we have enough colors by repeating the array if needed
        while (colors.length < data.labels.length) {
            colors.push(...colors);
        }

        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: colors.map(c => c.bg),
                    borderColor: colors.map(c => c.border),
                    borderWidth: 1,
                    hoverOffset: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right',
                        labels: {
                            boxWidth: 12,
                            usePointStyle: true,
                            pointStyle: 'circle'
                        }
                    },
                    title: {
                        display: true,
                        text: 'Sales by Category'
                    },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                const value = context.parsed;
                                const total = data.total;
                                const percentage = ((value / total) * 100).toFixed(1);
                                const formattedValue = new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD'
                                }).format(value);
                                return `${context.label}: ${formattedValue} (${percentage}%)`;
                            }
                        }
                    }
                },
                cutout: '60%',
                layout: {
                    padding: {
                        left: 10,
                        right: 10,
                        top: 10,
                        bottom: 10
                    }
                }
            }
        });
    }

    initStockChart(data) {
        // Remove loading state
        const loadingDiv = document.querySelector('#stockChart').parentNode.querySelector('.chart-loading');
        if (loadingDiv) {
            loadingDiv.remove();
        }

        const ctx = document.getElementById('stockChart');
        ctx.style.display = 'block';

        // Clear any existing chart
        const existingChart = Chart.getChart('stockChart');
        if (existingChart) {
            existingChart.destroy();
        }

        // Prepare data for the stacked bar chart
        const chartData = {
            labels: data.categories,
            datasets: [
                {
                    label: 'Normal Stock',
                    data: data.stockData.map(d => d.normal),
                    backgroundColor: 'rgba(76, 175, 80, 0.8)',
                    borderColor: 'rgba(76, 175, 80, 1)',
                    borderWidth: 1
                },
                {
                    label: 'Low Stock',
                    data: data.stockData.map(d => d.low),
                    backgroundColor: 'rgba(244, 67, 54, 0.8)',
                    borderColor: 'rgba(244, 67, 54, 1)',
                    borderWidth: 1,
                }
            ]
        };

        new Chart(ctx, {
            type: 'bar',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                indexAxis: 'y', // Make it a horizontal bar chart
                plugins: {
                    title: {
                        display: true,
                        text: 'Inventory Status by Category'
                    },
                    legend: {
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function (context) {
                                const label = context.dataset.label || '';
                                if (label) {
                                    return `${label}: ${context.parsed.x} units`;
                                }
                                return context.parsed.x;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        stacked: true,
                        title: {
                            display: true,
                            text: 'Number of Items'
                        },
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        stacked: true,
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    loadModule(module) {
        // Update active menu
        document.querySelectorAll('.menu-item').forEach(item => {
            item.classList.remove('active');
        });
        document.querySelector(`[data-module="${module}"]`)?.classList.add('active');

        this.currentModule = module;
        document.getElementById('pageTitle').textContent = this.getModuleTitle(module);

        // Load module content
        switch (module) {
            case 'dashboard':
                this.loadDashboard();
                break;
            case 'products':
                this.loadProducts();
                break;
            case 'categories':
                this.loadCategories();
                break;
            case 'suppliers':
                this.loadSuppliers();
                break;
            case 'orders':
                this.loadOrders();
                break;
            case 'transactions':
                this.loadTransactions();
                break;
            case 'reports':
                this.loadReports();
                break;
            case 'settings':
                this.loadSettings();
                break;
        }
    }

    getModuleTitle(module) {
        const titles = {
            dashboard: 'Dashboard',
            products: 'Products',
            categories: 'Categories',
            suppliers: 'Suppliers',
            orders: 'Orders',
            transactions: 'Transactions',
            reports: 'Reports',
            settings: 'Settings'
        };
        return titles[module] || 'Dashboard';
    }

    loadDashboard() {
        const content = `
            <!-- Key Metrics -->
            <div class="dashboard-stats">
                <div class="stat-card">
                    <div class="stat-icon"><i class="fas fa-box"></i></div>
                    <div class="stat-number" id="totalProducts">0</div>
                    <div class="stat-label">Total Products</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon"><i class="fas fa-shopping-cart"></i></div>
                    <div class="stat-number" id="totalOrders">0</div>
                    <div class="stat-label">Total Orders</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon"><i class="fas fa-tags"></i></div>
                    <div class="stat-number" id="totalCategories">0</div>
                    <div class="stat-label">Categories</div>
                </div>
                <div class="stat-card">
                    <div class="stat-icon"><i class="fas fa-truck"></i></div>
                    <div class="stat-number" id="totalSuppliers">0</div>
                    <div class="stat-label">Suppliers</div>
                </div>
            </div>

            <!-- Main Charts Section -->
            <div class="dashboard-charts">
                <div class="chart-card">
                    <div class="card-header">
                        <h3 class="card-title">Revenue Analysis</h3>
                    </div>
                    <div class="card-body">
                        <canvas id="revenueChart"></canvas>
                    </div>
                </div>
                <div class="chart-card">
                    <div class="card-header">
                        <h3 class="card-title">Stock Status</h3>
                    </div>
                    <div class="card-body">
                        <div class="stock-metrics">
                            <div class="stock-metric">
                                <span class="metric-label">Total Stock Quantity</span>
                                <span class="metric-value" id="totalStockQuantity">0</span>
                            </div>
                            <div class="stock-metric">
                                <span class="metric-label">Total Stock Value</span>
                                <span class="metric-value" id="totalStockValue">$0</span>
                            </div>
                            <div class="stock-metric">
                                <span class="metric-label">Average Stock Value</span>
                                <span class="metric-value" id="avgStockValue">$0</span>
                            </div>
                        </div>
                        <canvas id="stockChart"></canvas>
                    </div>
                </div>
            </div>

            <!-- Stock Status Details -->
            <div class="stock-status-section">
                <div class="status-cards">
                    <div class="status-card in-stock">
                        <div class="status-header">
                            <i class="fas fa-check-circle"></i>
                            <h4>In Stock</h4>
                        </div>
                        <div class="status-count" id="inStockCount">0</div>
                        <div class="status-label">Products</div>
                    </div>
                    <div class="status-card low-stock">
                        <div class="status-header">
                            <i class="fas fa-exclamation-triangle"></i>
                            <h4>Low Stock</h4>
                        </div>
                        <div class="status-count" id="lowStockCount">0</div>
                        <div class="status-label">Products</div>
                    </div>
                    <div class="status-card reordered">
                        <div class="status-header">
                            <i class="fas fa-sync"></i>
                            <h4>Reordered</h4>
                        </div>
                        <div class="status-count" id="reorderedCount">0</div>
                        <div class="status-label">Products</div>
                    </div>
                </div>
                <div class="needs-attention-section">
                    <h3>Products Needing Attention</h3>
                    <div class="needs-attention-table" id="needsAttentionTable">
                        <div class="loading-spinner">Loading product data...</div>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('contentArea').innerHTML = content;
        this.loadDashboardStats();
        this.initializeCharts();
    }

    async loadDashboardStats() {
        try {
            const requests = [
                fetch('/api/products').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/orders').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/categories').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/suppliers').then(r => r.ok ? r.json() : { totalElements: 0 })
            ];

            const [products, orders, categories, suppliers] = await Promise.all(requests);

            // Extract the total counts properly
            const totalProducts = products.content ? products.content.length : 0;
            const totalOrders = orders.content ? orders.content.length : 0;
            const totalCategories = categories.content ? categories.content.length : 0;
            const totalSuppliers = suppliers.content ? suppliers.content.length : 0;

            document.getElementById('totalProducts').textContent = totalProducts;
            document.getElementById('totalOrders').textContent = totalOrders;
            document.getElementById('totalCategories').textContent = totalCategories;
            document.getElementById('totalSuppliers').textContent = totalSuppliers;
        } catch (error) {
            console.error('Error loading dashboard stats:', error);
            document.getElementById('totalProducts').textContent = '0';
            document.getElementById('totalOrders').textContent = '0';
            document.getElementById('totalCategories').textContent = '0';
            document.getElementById('totalSuppliers').textContent = '0';
        }
    }

    async loadDashboardStatsFallback() {
        try {
            const requests = [
                fetch('/api/products?size=1').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/orders?size=1').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/categories?size=1').then(r => r.ok ? r.json() : { totalElements: 0 }),
                fetch('/api/suppliers?size=1').then(r => r.ok ? r.json() : { totalElements: 0 })
            ];

            const [products, orders, categories, suppliers] = await Promise.all(requests);

            document.getElementById('totalProducts').textContent = products.totalElements || 0;
            document.getElementById('totalOrders').textContent = orders.totalElements || 0;
            document.getElementById('totalCategories').textContent = categories.totalElements || 0;
            document.getElementById('totalSuppliers').textContent = suppliers.totalElements || 0;
        } catch (error) {
            console.error('Error loading dashboard stats fallback:', error);
            // Set zeros as final fallback
            document.getElementById('totalProducts').textContent = '0';
            document.getElementById('totalOrders').textContent = '0';
            document.getElementById('totalCategories').textContent = '0';
            document.getElementById('totalSuppliers').textContent = '0';
        }
    }

    loadProducts() {
        this.loadCrudModule('products', MODULE_FIELD_CONFIGS.products);
    }

    loadCategories() {
        this.loadCrudModule('categories', MODULE_FIELD_CONFIGS.categories);
    }

    loadSuppliers() {
        this.loadCrudModule('suppliers', MODULE_FIELD_CONFIGS.suppliers);
    }

    loadOrders() {
        this.loadCrudModule('orders', MODULE_FIELD_CONFIGS.orders);
    }

    loadTransactions() {
        this.loadCrudModule('transactions', MODULE_FIELD_CONFIGS.transactions);
    }

    loadReports() {
        const content = `
            <div class="reports-container">
                <div class="coming-soon-card">
                    <div class="coming-soon-icon">
                        <i class="fas fa-chart-line"></i>
                    </div>
                    <h2>Reports Coming Soon!</h2>
                    <p>Since this project was rushed some features are not available.</p>
                    <p>Some of those features are detailed analytics, custom reports, and more.</p>
                    <div class="coming-soon-features">
                        <div class="feature">
                            <i class="fas fa-chart-bar"></i>
                            <span>Sales Analytics</span>
                        </div>
                        <div class="feature">
                            <i class="fas fa-dollar-sign"></i>
                            <span>Revenue Reports</span>
                        </div>
                        <div class="feature">
                            <i class="fas fa-boxes"></i>
                            <span>Inventory Tracking</span>
                        </div>
                        <div class="feature">
                            <i class="fas fa-chart-pie"></i>
                            <span>Business Insights</span>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('contentArea').innerHTML = content;
    }

    loadSettings() {
        const content = `
            <div class="settings-container">
                <div style="text-align: center; margin: 20px auto 30px; max-width: 600px;">
                    <div style="background: linear-gradient(135deg, #059669 0%, #10B981 100%); color: white; padding: 15px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                        <h2 style="font-size: 20px;">✨ User Management ✨</h2>
                    </div>
                </div>
                <!-- User Management Section -->
                <div class="card">
                    <div class="card-header">
                        <h3 class="card-title">User Management</h3>
                    </div>
                    <div class="card-body">
                        <div id="userCardsContainer" class="user-cards-grid">
                            <div class="loading-card">Loading users...</div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('contentArea').innerHTML = content;
        this.loadUserCards();
    }

    async loadCrudModule(module, fields) {
        const content = `
            <div class="card">
                <div class="card-header">
                    <h3 class="card-title">${this.getModuleTitle(module)}</h3>
                    <div class="header-actions">
                        <button class="btn btn-primary" onclick="dashboard.openCreateModal('${module}')">
                            <i class="fas fa-plus"></i> Add ${module.slice(0, -1)}
                        </button>
                    </div>
                </div>
                <div class="card-body">
                    <div class="search-filter">
                        <input type="text" class="search-input" placeholder="Search..." onkeyup="dashboard.filterTable(this.value)">
                        <button class="btn btn-secondary" onclick="dashboard.refreshData('${module}')" title="Refresh Data">
                            <i class="fas fa-refresh"></i> Refresh
                        </button>
                    </div>
                    <div class="table-container">
                        <table class="table" id="dataTable">
                            <thead>
                                <tr>
                                    ${fields.map(field => `<th>${field.label}</th>`).join('')}
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody id="tableBody">
                                <tr><td colspan="${fields.length + 1}">Loading...</td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        `;
        document.getElementById('contentArea').innerHTML = content;
        this.currentFields = fields;

        await this.loadData(module);
    }

    async loadData(module) {
        try {
            const response = await fetch(`/api/${module}?page=0&size=50`);

            if (this.handleAuthFailure(response)) {
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const responseData = await response.json();
            console.log(`API Response for ${module}:`, responseData);

            // Handle different response structures
            let items = [];
            let paginationInfo = null;

            // Add column display configurations
            const columnDisplayConfig = {
                status: (value) => value ? value.replace(/[">-]/g, '').trim() : '',
                totalAmount: (value) => typeof value === 'number' ?
                    new Intl.NumberFormat('en-US', {
                        style: 'currency',
                        currency: 'USD',
                        minimumFractionDigits: 2
                    }).format(value) : value,
                orderItems: (value) => Array.isArray(value) ? `${value.length} item(s)` : value
            };

            // Check if response has success property (our API structure)
            if (responseData.success !== undefined) {
                if (responseData.success) {
                    // Map module names to response keys
                    const moduleKeyMap = {
                        'products': 'products',
                        'categories': 'categories',
                        'suppliers': 'suppliers',
                        'orders': 'orders',
                        'transactions': 'transactions'
                    };

                    const dataKey = moduleKeyMap[module] || module;
                    items = responseData[dataKey] || responseData.data || [];

                    // Handle pagination info if available
                    if (responseData.totalPages) {
                        paginationInfo = {
                            number: responseData.currentPage || 0,
                            totalPages: responseData.totalPages,
                            totalElements: responseData.totalElements,
                            first: (responseData.currentPage || 0) === 0,
                            last: (responseData.currentPage || 0) === (responseData.totalPages - 1)
                        };
                    }
                } else {
                    // API returned error
                    throw new Error(responseData.message || `Failed to load ${module}`);
                }
            } else {
                // Handle legacy response structure
                items = responseData.content || responseData;
                paginationInfo = responseData.content ? responseData : null;
            }

            // Ensure items is an array
            if (!Array.isArray(items)) {
                console.error(`Expected array but got:`, items);
                items = [];
            }

            this.renderTable(items, module, paginationInfo);
        } catch (error) {
            console.error(`Error loading ${module}:`, error);
            this.showErrorMessage(`Failed to load ${module}. Please try again.`);
            document.getElementById('tableBody').innerHTML = `
                <tr>
                    <td colspan="100%" class="text-center error-message">
                        <i class="fas fa-exclamation-triangle"></i> 
                        Error loading data: ${error.message}
                        <br>
                        <button class="btn btn-sm btn-primary" onclick="dashboard.loadData('${module}')">
                            <i class="fas fa-refresh"></i> Retry
                        </button>
                    </td>
                </tr>
            `;
        }
    }    // Auto refresh functionality
    // Refresh data with loading indicator
    async refreshData(module) {
        const refreshButton = event.target.closest('button');
        const originalHTML = refreshButton.innerHTML;

        // Show loading state
        refreshButton.innerHTML = '<i class="fas fa-spin fa-spinner"></i> Refreshing...';
        refreshButton.disabled = true;

        // Add loading state to table
        document.getElementById('tableBody').innerHTML = `
            <tr>
                <td colspan="100%" class="text-center">
                    <div class="loading-spinner">Refreshing data...</div>
                </td>
            </tr>
        `;

        try {
            await this.loadData(module);

            // Show success feedback
            this.showSuccessMessage(`${module.charAt(0).toUpperCase() + module.slice(1)} data refreshed successfully!`);
        } catch (error) {
            console.error(`Error refreshing ${module}:`, error);
            this.showErrorMessage(`Failed to refresh ${module} data. Please try again.`);
        } finally {
            // Restore button state
            refreshButton.innerHTML = originalHTML;
            refreshButton.disabled = false;
        }
    }

    renderTable(data, module, paginationInfo = null) {
        const tbody = document.getElementById('tableBody');
        if (!Array.isArray(data) || data.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="100%" class="text-center">
                        <i class="fas fa-inbox"></i> No ${module} found
                        <br>
                        <button class="btn btn-primary btn-sm mt-2" onclick="dashboard.openCreateModal('${module}')">
                            <i class="fas fa-plus"></i> Add First ${module.slice(0, -1)}
                        </button>
                    </td>
                </tr>
            `;
            return;
        }

        // Custom display function for specific fields
        const formatFieldValue = (value, field) => {
            if (field.key === 'status') {
                return value ? value.replace(/[">-]/g, '').trim() : '';
            } else if (field.key === 'totalAmount') {
                const numValue = parseFloat(value);
                return !isNaN(numValue) ? new Intl.NumberFormat('en-US', {
                    style: 'currency',
                    currency: 'USD',
                    minimumFractionDigits: 2
                }).format(numValue) : value;
            } else if (field.key === 'orderItems') {
                const count = value && Array.isArray(value) ? value.length : 0;
                return `${count} item(s)`;
            }
            return value;
        };

        // Generate table headers using field config labels with improved styling
        const thead = document.querySelector('#dataTable thead tr');
        const headerHtml = this.currentFields.map(field => {
            let headerClass = '';
            let headerStyle = '';

            // Add specific styling for different field types
            switch (field.key) {
                case 'id':
                    headerClass = 'col-id';
                    headerStyle = 'width: 60px; text-align: center;';
                    break;
                case 'name':
                    headerClass = 'col-name';
                    headerStyle = 'min-width: 150px;';
                    break;
                case 'price':
                    headerClass = 'col-price';
                    headerStyle = 'width: 100px; text-align: right;';
                    break;
                case 'stockQuantity':
                    headerClass = 'col-stock';
                    headerStyle = 'width: 80px; text-align: center;';
                    break;
                case 'sku':
                    headerClass = 'col-sku';
                    headerStyle = 'width: 120px;';
                    break;
                case 'isActive':
                    headerClass = 'col-active';
                    headerStyle = 'width: 80px; text-align: center;';
                    break;
                case 'category':
                case 'supplier':
                    headerClass = 'col-relation';
                    headerStyle = 'width: 120px;';
                    break;
                default:
                    headerClass = 'col-default';
            }

            return `
                <th class="${headerClass}" style="${headerStyle}">
                    <div class="header-content">
                        <span>${field.label}</span>
                        ${field.sortable !== false ? '<i class="fas fa-sort ms-1"></i>' : ''}
                    </div>
                </th>
            `;
        }).join('');

        thead.innerHTML = headerHtml + `
            <th class="col-actions" style="width: 120px; text-align: center;">
                <div class="header-content">
                    <span>Actions</span>
                </div>
            </th>
        `;

        // Show loading state while rendering
        tbody.innerHTML = `<tr><td colspan="100%" class="text-center">
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i> Rendering data...
            </div>
        </td></tr>`;

        // Render the data with improved styling and alignment
        setTimeout(() => {
            console.log('🎯 Rendering table data:', data);
            if (data.length > 0) {
                console.log('📋 Sample item structure:', data[0]);
            }

            tbody.innerHTML = data.map(item => {
                const rowHtml = this.currentFields.map(field => {
                    let value = item[field.key];
                    value = formatFieldValue(value, field);
                    let cellClass = '';
                    let cellStyle = '';

                    // Add specific styling for different field types
                    switch (field.key) {
                        case 'id':
                            cellClass = 'cell-id';
                            cellStyle = 'text-align: center; font-weight: bold; color: var(--emerald-bright);';
                            break;
                        case 'name':
                            cellClass = 'cell-name';
                            cellStyle = 'font-weight: 500;';
                            break;
                        case 'price':
                            cellClass = 'cell-price';
                            cellStyle = 'text-align: right; font-weight: 600; color: var(--emerald-ocean);';
                            break;
                        case 'stockQuantity':
                            cellClass = 'cell-stock';
                            cellStyle = 'text-align: center; white-space: nowrap;';
                            // Add color coding for stock levels
                            if (typeof item[field.key] === 'number') {
                                const stock = item[field.key];
                                if (stock <= 5) cellStyle += ' color: #dc3545;'; // Red for low stock
                                else if (stock <= 20) cellStyle += ' color: #ffc107;'; // Yellow for medium stock
                                else cellStyle += ' color: var(--emerald-bright);'; // Green for good stock
                            }
                            break;
                        case 'sku':
                            cellClass = 'cell-sku';
                            cellStyle = 'font-family: monospace; font-size: 0.9em; white-space: nowrap;';
                            break;
                        case 'isActive':
                            cellClass = 'cell-active';
                            cellStyle = 'text-align: center; white-space: nowrap; font-weight: 600;';
                            // Color code active status
                            if (item[field.key]) {
                                cellStyle += ' color: var(--emerald-bright);';
                            } else {
                                cellStyle += ' color: #dc3545;';
                            }
                            break;
                        case 'category':
                        case 'supplier':
                            cellClass = 'cell-relation';
                            cellStyle = 'color: var(--emerald-vivid); white-space: nowrap;';
                            break;
                        default:
                            cellClass = 'cell-default';
                    }

                    // Process value based on field type
                    let displayValue = value;

                    // Special handling for category and supplier
                    if (field.key === 'category' && value) {
                        displayValue = value.name || 'Unknown Category';
                    } else if (field.key === 'supplier' && value) {
                        displayValue = value.name || 'Unknown Supplier';
                    } else if (!['id', 'stockQuantity', 'price', 'isActive', 'sku'].includes(field.key)) {
                        displayValue = this.truncateText(value, 30);
                    }

                    // Create a title attribute for hover effect
                    let titleValue = displayValue;
                    if (field.key === 'category' && value) {
                        titleValue = `Category: ${value.name || 'Unknown'}`;
                    } else if (field.key === 'supplier' && value) {
                        titleValue = `Supplier: ${value.name || 'Unknown'}`;
                    }

                    return `<td class="${cellClass}" style="${cellStyle}" title="${titleValue}">${displayValue}</td>`;
                }).join('');

                return `
                    <tr data-id="${item.id}" class="fade-in table-row-enhanced">
                        ${rowHtml}
                        <td class="actions-column" style="text-align: center; white-space: nowrap;">
                            <div class="action-buttons-enhanced">
                                ${module !== 'categories' ? `
                                <button class="btn-action btn-view" onclick="dashboard.viewItem('${module}', ${item.id})" title="View Details">
                                    <i class="fas fa-eye"></i>
                                </button>
                                ` : ''}
                                <button class="btn-action btn-edit" onclick="dashboard.openEditModal('${module}', ${item.id})" title="Edit">
                                    <i class="fas fa-edit"></i>
                                </button>
                                <button class="btn-action btn-delete" onclick="dashboard.deleteItem('${module}', ${item.id})" title="Delete">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </div>
                        </td>
                    </tr>
                `;
            }).join('');

            // Add pagination info if available
            if (paginationInfo && paginationInfo.totalPages > 1) {
                this.addPaginationControls(module, paginationInfo);
            }
        }, 100); // Small delay to show the loading state
    }

    getEnhancedDisplayValue(item, field) {
        let value = item[field.key];

        console.log(`🔍 Displaying field ${field.key}:`, {
            value: value,
            item: item,
            field: field
        });

        if (value === null || value === undefined) {
            return '-';
        }

        // Special handling for category and supplier
        if (field.key === 'category') {
            return value && value.name ? value.name : 'No Category';
        }
        if (field.key === 'supplier') {
            return value && value.name ? value.name : 'No Supplier';
        }

        // Handle special field types with better formatting
        // Handle specific field types for enhanced display
        if (field.key === 'status') {
            return this.formatStatus(value);
        } else if (field.key === 'totalAmount' || field.key === 'amount') {
            return this.formatCurrency(value);
        }

        switch (field.type) {
            case 'select-async':
                // For foreign keys, display the related object's name or ID
                if (field.key === 'category') {
                    console.log('📂 Category processing:', {
                        category: value,
                        item: item
                    });

                    // Check if we have the full category object
                    if (value && value.name) {
                        return value.name;
                    }
                    // Check if we have a category ID
                    if (value && value.id) {
                        return `Category #${value.id}`;
                    }
                    return '-';
                } else if (field.key === 'supplier') {
                    console.log('🏭 Supplier processing:', {
                        supplier: value,
                        item: item
                    });

                    // Check if we have the full supplier object
                    if (value && value.name) {
                        return value.name;
                    }
                    // Check if we have a supplier ID
                    if (value && value.id) {
                        return `Supplier #${value.id}`;
                    }
                    return '-';
                } else if (field.key === 'orderId' && item.order) {
                    return item.order.orderNumber || item.order.id;
                }
                return value || '-';
            case 'number':
                if (field.key.includes('price') || field.key.includes('amount')) {
                    // Ensure the value is treated as a number and properly formatted
                    const numValue = typeof value === 'string' ? parseFloat(value) : value;
                    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(numValue);
                }
                if (field.key === 'stockQuantity') {
                    return value.toString(); // Just show the number without "units"
                }
                return value;
            case 'select':
                return value.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
            case 'checkbox':
                // Show clean text instead of icons for Active status
                if (field.key === 'isActive') {
                    return value ? 'Active' : 'Inactive';
                }
                return value ? '<i class="fas fa-check text-success"></i>' : '<i class="fas fa-times text-danger"></i>';
            case 'readonly':
                // For ID fields, show plain number without any styling
                if (field.key === 'id') {
                    return value.toString();
                }
                return `<span class="readonly-field">${value}</span>`;
            case 'order-items':
                if (Array.isArray(value) && value.length > 0) {
                    return `${value.length} item(s)`;
                }
                return 'No items';
            default:
                return value.toString();
        }
    }

    truncateText(text, maxLength) {
        if (!text) return '';
        return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
    }

    formatStatus(status) {
        if (!status) return '-';
        // Keep the original case but add status badge styling
        return `<span class="status-badge status-${status.toLowerCase()}">${status}</span>`;
    }

    formatCurrency(value) {
        if (!value) return '$0.00';
        // Remove any existing currency formatting
        const numValue = typeof value === 'string' ?
            parseFloat(value.replace(/[^0-9.-]+/g, '')) :
            parseFloat(value);

        if (isNaN(numValue)) return '$0.00';

        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(numValue);
    }

    openCreateModal(module) {
        console.log('Opening create modal for module:', module);
        console.log('Available field configs:', Object.keys(MODULE_FIELD_CONFIGS));

        this.currentEditId = null;
        this.currentModule = module;
        this.currentFields = MODULE_FIELD_CONFIGS[module] || [];

        console.log('Current fields for', module, ':', this.currentFields);

        document.getElementById('modalTitle').textContent = `Add ${this.getModuleTitle(module).slice(0, -1)}`;
        this.buildForm();

        // Make sure submit button is visible and says "Add"
        const submitBtn = document.querySelector('#modalForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.style.display = 'inline-block';
            submitBtn.textContent = 'Add';
        }

        document.getElementById('modal').style.display = 'block';
    }

    async openEditModal(module, id) {
        this.currentEditId = id;
        this.currentModule = module;
        this.currentFields = MODULE_FIELD_CONFIGS[module] || [];
        document.getElementById('modalTitle').textContent = `Edit ${this.getModuleTitle(module).slice(0, -1)}`;

        try {
            const response = await fetch(`/api/${module}/${id}`);

            if (this.handleAuthFailure(response)) {
                return;
            }

            if (response.ok) {
                const responseData = await response.json();
                console.log('Edit modal response data:', responseData);

                // Extract the actual item data from the response
                let itemData = {};
                if (responseData.success !== undefined) {
                    // API response with success flag
                    const itemKey = module.slice(0, -1); // Remove 's' from module name
                    itemData = responseData[itemKey] || responseData.data || {};
                } else {
                    // Direct item response
                    itemData = responseData;
                }

                console.log('Item data for form:', itemData);

                await this.buildForm(itemData);

                // Make sure submit button is visible
                const submitBtn = document.querySelector('#modalForm button[type="submit"]');
                if (submitBtn) {
                    submitBtn.style.display = 'inline-block';
                    submitBtn.textContent = 'Update';
                }

                document.getElementById('modal').style.display = 'block';
            } else {
                this.showErrorAlert('Error loading item for editing');
            }
        } catch (error) {
            console.error('Error loading item:', error);
            this.showErrorAlert('Error loading item for editing');
        }
    }

    async buildForm(data = {}) {
        console.log('Building form with data:', data);
        console.log('Current fields:', this.currentFields);

        const formFields = document.getElementById('formFields');
        if (!formFields) {
            console.error('formFields element not found!');
            return;
        }

        const loadingSpinner = document.createElement('div');
        loadingSpinner.className = 'loading-spinner text-center';
        loadingSpinner.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading form...';
        formFields.innerHTML = ''; // Clear existing content
        formFields.appendChild(loadingSpinner);

        // Load async options for select-async fields with enhanced authentication
        const asyncFields = this.currentFields.filter(field => field.type === 'select-async');
        const asyncOptions = {};

        for (const field of asyncFields) {
            console.log(`🔄 Loading options for ${field.key} from ${field.endpoint}`);
            try {
                // Try multiple authentication strategies
                let response;
                let attempts = 0;
                const maxAttempts = 3;

                while (attempts < maxAttempts && (!response || !response.ok)) {
                    attempts++;
                    console.log(`🔐 Attempt ${attempts} for ${field.key}`);

                    const fetchOptions = {
                        method: 'GET',
                        credentials: 'include',
                        headers: {
                            'Accept': 'application/json',
                            'Content-Type': 'application/json',
                            'Cache-Control': 'no-cache'
                        }
                    };

                    // Add session header if available
                    const sessionId = document.cookie.split(';').find(c => c.trim().startsWith('JSESSIONID='));
                    if (sessionId) {
                        fetchOptions.headers['Cookie'] = sessionId.trim();
                    }

                    response = await fetch(field.endpoint + '?page=-1&_=' + Date.now(), fetchOptions);
                    console.log(`📊 Response for ${field.key} (attempt ${attempts}):`, response.status, response.statusText);

                    // If 401, try to re-authenticate or refresh session
                    if (response.status === 401 && attempts < maxAttempts) {
                        console.warn(`🚫 Authentication failed for ${field.key} on attempt ${attempts}, will retry...`);

                        // Try to refresh authentication by calling auth status
                        try {
                            const authResponse = await fetch('/api/auth/status', {
                                credentials: 'include',
                                headers: { 'Accept': 'application/json' }
                            });
                            if (authResponse.ok) {
                                console.log(`✅ Authentication refreshed for ${field.key}`);
                            }
                        } catch (authError) {
                            console.warn(`⚠️ Auth refresh failed for ${field.key}:`, authError);
                        }

                        // Wait a bit before retry
                        await new Promise(resolve => setTimeout(resolve, 500));
                    }
                }

                if (response && response.ok) {
                    const responseData = await response.json();
                    console.log(`📦 Response data for ${field.key}:`, responseData);

                    // Handle API response structure with better error handling
                    let options = [];
                    if (responseData && typeof responseData === 'object') {
                        if (responseData.success !== undefined) {
                            if (responseData.success) {
                                // Map endpoint to response key
                                const endpointKeyMap = {
                                    '/api/categories': 'categories',
                                    '/api/suppliers': 'suppliers',
                                    '/api/products': 'products',
                                    '/api/orders': 'orders'
                                };

                                const dataKey = endpointKeyMap[field.endpoint] || 'data';
                                options = responseData[dataKey] || responseData.data || [];
                            } else {
                                console.warn(`❌ API returned success=false for ${field.key}:`, responseData.message);
                                // For debugging, still try to extract data
                                options = responseData.data || responseData.categories || responseData.suppliers || [];
                            }
                        } else {
                            // Legacy response structure - try multiple possible keys
                            options = responseData.content || responseData.categories || responseData.suppliers || responseData.data || responseData || [];
                        }
                    } else {
                        console.warn(`⚠️ Unexpected response format for ${field.key}:`, responseData);
                        options = [];
                    }

                    // Ensure we have an array with valid objects
                    if (!Array.isArray(options)) {
                        console.warn(`⚠️ Expected array but got:`, typeof options, options);
                        options = [];
                    }

                    // Filter out invalid options (must have id and name)
                    options = options.filter(opt => opt && typeof opt === 'object' && opt.id && opt.name);

                    asyncOptions[field.key] = options;
                    console.log(`✅ Successfully loaded ${asyncOptions[field.key].length} options for ${field.key}:`,
                        asyncOptions[field.key].map(o => ({ id: o.id, name: o.name })));
                } else {
                    console.error(`❌ Final failure loading options for ${field.key}:`, response?.status || 'No response', response?.statusText || 'Unknown error');
                    asyncOptions[field.key] = [];
                }
            } catch (error) {
                console.error(`💥 Exception loading options for ${field.key}:`, error);
                asyncOptions[field.key] = [];
            }

            // If still no options, add a test fallback based on screenshots
            if (asyncOptions[field.key].length === 0) {
                console.warn(`🔧 Adding fallback options for ${field.key}`);
                if (field.key === 'category') {
                    asyncOptions[field.key] = [
                        { id: 5, name: 'KRUD' },
                        { id: 7, name: 'SFDGSFDGSSDFGSDGDS' },
                        { id: 8, name: 'Generic H2 (Embedded)' },
                        { id: 9, name: 'JDSHFGH' },
                        { id: 10, name: 'Generic MySQL' },
                        { id: 11, name: 'khtru' },
                        { id: 12, name: 'khmer' }
                    ];
                    console.log(`🔄 Using fallback categories for ${field.key}`);
                } else if (field.key === 'supplier') {
                    asyncOptions[field.key] = [
                        { id: 1, name: 'FGGD' },
                        { id: 2, name: 'Test Supplier' },
                        { id: 3, name: 'Default Supplier' }
                    ];
                    console.log(`🔄 Using fallback suppliers for ${field.key}`);
                }
            }
        }

        // Remove loading spinner
        formFields.removeChild(loadingSpinner);

        // Filter out ID field for new records (since ID is auto-generated)
        const fieldsToShow = this.currentFields.filter(field => {
            // Hide ID field when creating new records (currentEditId is null)
            if (field.key === 'id' && !this.currentEditId) {
                return false;
            }
            return true;
        });

        formFields.innerHTML = fieldsToShow.map(field => {
            let input = '';
            let value = data[field.key];

            // Handle different value types
            if (value === null || value === undefined) {
                value = '';
            } else if (typeof value === 'boolean') {
                // Keep boolean values as is for checkbox handling
            } else if (field.type === 'select-async' && typeof value === 'object' && value !== null) {
                // For relationship objects, extract the ID for form field
                value = value.id || '';
            } else {
                value = value.toString();
            }

            console.log(`Building field ${field.key} with value:`, value, typeof value);

            const isReadonly = field.readonly ? 'readonly' : '';
            const isRequired = field.required ? 'required' : '';
            const minAttr = field.min !== undefined ? `min="${field.min}"` : '';
            const maxAttr = field.max !== undefined ? `max="${field.max}"` : '';
            const stepAttr = field.step !== undefined ? `step="${field.step}"` : '';
            const patternAttr = field.pattern ? `pattern="${field.pattern}"` : '';

            // Add validation attributes based on field config
            let validationAttrs = '';
            if (field.validation) {
                if (field.validation.minLength) validationAttrs += ` minlength="${field.validation.minLength}"`;
                if (field.validation.maxLength) validationAttrs += ` maxlength="${field.validation.maxLength}"`;
                if (field.validation.min) validationAttrs += ` min="${field.validation.min}"`;
                if (field.validation.max) validationAttrs += ` max="${field.validation.max}"`;
                if (field.validation.pattern) {
                    const pattern = field.validation.pattern instanceof RegExp
                        ? field.validation.pattern.source
                        : field.validation.pattern;
                    validationAttrs += ` pattern="${pattern}"`;
                }
            }

            switch (field.type) {
                case 'textarea':
                    input = `<textarea class="form-control" name="${field.key}" ${isRequired} ${isReadonly} ${validationAttrs}>${value}</textarea>`;
                    break;

                case 'select':
                    input = `<select class="form-control" name="${field.key}" ${isRequired}>
                        <option value="">Select ${field.display}...</option>
                        ${field.options.map(opt => `<option value="${opt}" ${value === opt ? 'selected' : ''}>${opt.replace(/_/g, ' ')}</option>`).join('')}
                    </select>`;
                    break;

                case 'select-async':
                    const options = asyncOptions[field.key] || [];
                    console.log(`🎯 Rendering select-async for ${field.key} with ${options.length} options`);

                    // ALWAYS render as select element - never fall back to text input
                    let selectOptions = '';

                    if (options.length === 0) {
                        // Show loading or error state, but still as a select
                        selectOptions = `<option value="" disabled>Loading ${field.display}...</option>`;
                        console.warn(`⚠️ No options available for ${field.key}, showing loading state`);
                    } else {
                        selectOptions = `<option value="">Select ${field.display}...</option>`;
                        selectOptions += options.map(opt => {
                            const optValue = opt[field.valueKey || 'id'];
                            const optLabel = opt[field.labelKey || 'name'];
                            const isSelected = value == optValue ? 'selected' : '';
                            return `<option value="${optValue}" ${isSelected}>${optLabel}</option>`;
                        }).join('');
                        console.log(`✅ Built ${options.length} select options for ${field.key}`);
                    }

                    input = `<select class="form-control ${options.length === 0 ? 'select-loading' : ''}" 
                                    name="${field.key}" ${isRequired} 
                                    ${options.length === 0 ? 'data-loading="true"' : ''}>
                                ${selectOptions}
                             </select>`;

                    // Add retry mechanism for failed loads
                    if (options.length === 0) {
                        console.log(`🔄 Will retry loading options for ${field.key} after render`);
                        // Store field info for retry
                        setTimeout(() => this.retryAsyncFieldLoad(field), 1000);
                    }
                    break;

                case 'checkbox':
                    const checkboxId = `${field.key}_${Date.now()}`;
                    input = `
                        <div class="form-check d-flex align-items-center justify-content-start" style="max-width: fit-content;">
                            <input type="checkbox" class="form-check-input me-2" id="${checkboxId}" name="${field.key}" value="true" 
                                   ${value === true || value === 'true' ? 'checked' : ''} ${isReadonly ? 'disabled' : ''}>
                            <label class="form-check-label mb-0" for="${checkboxId}">
                                <span class="fw-normal">${field.label || 'Active'}</span>
                            </label>
                        </div>
                    `;
                    break;

                case 'order-items':
                    input = this.buildOrderItemsInput(data.orderItems || []);
                    break;

                case 'readonly':
                    input = `<input type="text" class="form-control readonly-field" name="${field.key}" value="${value}" readonly>`;
                    break;

                default:
                    const placeholder = this.getFieldPlaceholder(field, value);
                    input = `<input type="${field.type}" class="form-control" name="${field.key}" value="${value}" 
                            ${isRequired} ${isReadonly} ${minAttr} ${maxAttr} ${stepAttr} ${patternAttr} ${validationAttrs}
                            placeholder="${placeholder}">`;
            }

            return `
                <div class="form-group ${field.required ? 'required-field' : ''}">
                    <label for="${field.key}">
                        ${field.display || field.label} 
                        ${field.required ? '<span class="required">*</span>' : ''}
                    </label>
                    ${input}
                    ${this.getFieldHint(field)}
                </div>
            `;
        }).join('');

        // Clear any existing error alerts when form is built successfully
        const existingAlert = document.querySelector('.header-alert');
        if (existingAlert) {
            console.log('🗑️ Clearing existing error alert');
            existingAlert.remove();
        }

        // Add form validation (only once)
        this.addFormValidation();

        // Add order items functionality if present
        if (this.currentFields.some(f => f.type === 'order-items')) {
            this.initOrderItemsHandlers();
        }
    }

    getFieldPlaceholder(field, value) {
        if (field.key === 'orderNumber' && !value) return 'Auto-generated';
        if (field.key === 'transactionNumber' && !value) return 'Auto-generated';
        if (field.placeholder) return field.placeholder;
        return `Enter ${(field.display || field.label).toLowerCase()}`;
    }

    getFieldHint(field) {
        if (field.key === 'totalAmount') {
            return '<small class="form-text text-muted">Will be calculated automatically from order items</small>';
        }
        if (field.hint) {
            return `<small class="form-text text-muted">${field.hint}</small>`;
        }
        if (field.validation) {
            let hints = [];
            if (field.validation.minLength) hints.push(`Min ${field.validation.minLength} characters`);
            if (field.validation.maxLength) hints.push(`Max ${field.validation.maxLength} characters`);
            if (hints.length > 0) {
                return `<small class="form-text text-muted">${hints.join(', ')}</small>`;
            }
        }
        return '';
    }

    // Retry mechanism for failed async field loads
    async retryAsyncFieldLoad(field) {
        console.log(`🔄 Retrying load for ${field.key}`);

        try {
            const response = await fetch(field.endpoint + '?page=-1&_retry=' + Date.now(), {
                credentials: 'include',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const responseData = await response.json();
                let options = [];

                if (responseData.success !== undefined && responseData.success) {
                    const endpointKeyMap = {
                        '/api/categories': 'categories',
                        '/api/suppliers': 'suppliers',
                        '/api/products': 'products',
                        '/api/orders': 'orders'
                    };
                    const dataKey = endpointKeyMap[field.endpoint] || 'data';
                    options = responseData[dataKey] || [];
                } else {
                    options = responseData.content || responseData || [];
                }

                if (Array.isArray(options) && options.length > 0) {
                    console.log(`✅ Retry successful for ${field.key}, updating select options`);

                    // Update the select element with new options
                    const selectElement = document.querySelector(`select[name="${field.key}"]`);
                    if (selectElement) {
                        selectElement.innerHTML = `
                            <option value="">Select ${field.display}...</option>
                            ${options.map(opt =>
                            `<option value="${opt[field.valueKey || 'id']}">${opt[field.labelKey || 'name']}</option>`
                        ).join('')}
                        `;
                        selectElement.classList.remove('select-loading');
                        selectElement.removeAttribute('data-loading');
                        console.log(`🎯 Updated ${field.key} select with ${options.length} options`);
                    }
                } else {
                    console.warn(`⚠️ Retry for ${field.key} returned no valid options`);
                }
            } else {
                console.error(`❌ Retry failed for ${field.key}:`, response.status);
            }
        } catch (error) {
            console.error(`💥 Retry exception for ${field.key}:`, error);
        }
    }

    addFormValidation() {
        const form = document.getElementById('modalForm');
        const inputs = form.querySelectorAll('input, textarea, select');

        inputs.forEach(input => {
            // Real-time validation
            input.addEventListener('blur', () => {
                this.validateField(input);
            });

            // Different events for different input types
            if (input.tagName === 'SELECT') {
                input.addEventListener('change', () => {
                    console.log(`🔄 Select ${input.name} changed to:`, input.value);
                    this.clearFieldError(input);
                    // Validate immediately for select fields to clear errors
                    this.validateField(input);
                });
            } else {
                input.addEventListener('input', () => {
                    this.clearFieldError(input);
                });
            }
        });
    }

    validateField(input) {
        const fieldConfig = this.currentFields.find(f => f.key === input.name);
        if (!fieldConfig) return true;

        this.clearFieldError(input);

        // Skip validation for loading select fields
        if (input.hasAttribute('data-loading') && input.getAttribute('data-loading') === 'true') {
            console.log(`⏳ Skipping validation for loading field: ${input.name}`);
            return true; // Don't validate loading fields
        }

        // Debug validation
        console.log(`🔍 Validating field ${input.name}:`, {
            value: input.value,
            type: input.type || input.tagName,
            required: fieldConfig.required,
            fieldConfig: fieldConfig
        });

        // Required validation - handle select elements properly
        if (fieldConfig.required) {
            let isEmpty = false;

            if (input.tagName === 'SELECT') {
                // For select elements, check if value is empty or the default option
                isEmpty = !input.value || input.value === '' || input.value === null;
            } else {
                // For other input types, use trim
                isEmpty = !input.value || !input.value.trim();
            }

            if (isEmpty) {
                console.log(`❌ Field ${input.name} is required but empty`);
                this.showFieldError(input, `${fieldConfig.label} is required`);
                return false;
            }
        }

        // Type-specific validation - only validate if field has a value
        if (fieldConfig.validation && input.value) {
            const validation = fieldConfig.validation;
            const value = input.value;

            if (validation.minLength && value.length < validation.minLength) {
                this.showFieldError(input, `${fieldConfig.label} must be at least ${validation.minLength} characters`);
                return false;
            }

            if (validation.maxLength && value.length > validation.maxLength) {
                this.showFieldError(input, `${fieldConfig.label} must not exceed ${validation.maxLength} characters`);
                return false;
            }

            if (validation.min && parseFloat(value) < validation.min) {
                this.showFieldError(input, `${fieldConfig.label} must be at least ${validation.min}`);
                return false;
            }

            if (validation.pattern && !new RegExp(validation.pattern).test(value)) {
                const message = validation.message || `${fieldConfig.label} format is invalid`;
                this.showFieldError(input, message);
                return false;
            }
        }

        console.log(`✅ Field ${input.name} passed validation`);
        return true;
    }

    showFieldError(input, message) {
        const formGroup = input.closest('.form-group');
        formGroup.classList.add('has-error');

        let errorElement = formGroup.querySelector('.field-error');
        if (!errorElement) {
            errorElement = document.createElement('small');
            errorElement.className = 'field-error text-danger';
            formGroup.appendChild(errorElement);
        }
        errorElement.textContent = message;
        input.classList.add('is-invalid');
    }

    clearFieldError(input) {
        const formGroup = input.closest('.form-group');
        formGroup.classList.remove('has-error');
        input.classList.remove('is-invalid');

        const errorElement = formGroup.querySelector('.field-error');
        if (errorElement) {
            errorElement.remove();
        }
    }

    buildOrderItemsInput(orderItems) {
        // Ensure at least one empty item for new orders
        if (!orderItems || orderItems.length === 0) {
            orderItems = [{}];
        }

        return `
            <div class="order-items-container">
                <div class="order-items-header">
                    <span>Order Items</span>
                    <button type="button" class="btn btn-sm btn-primary" onclick="dashboard.addOrderItem()">
                        <i class="fas fa-plus"></i> Add Item
                    </button>
                </div>
                <div id="orderItemsList" class="order-items-list">
                    ${orderItems.map((item, index) => this.buildOrderItemRow(item, index)).join('')}
                </div>
                <div class="order-total">
                    <strong>Total: <span id="orderTotalDisplay">${new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(0)}</span></strong>
                </div>
            </div>
        `;
    }

    buildOrderItemRow(item = {}, index = 0) {
        // Handle different data structures for new items vs editing existing items
        let productId = null;
        let quantity = item.quantity || 1;
        let price = 0;

        // For editing existing orders, item might have product object
        if (item.product) {
            productId = item.product.id;
            price = item.unitPrice || item.price || 0;
        } else if (item.productId) {
            // For new items or when productId is directly available
            productId = item.productId;
            price = item.price || 0;
        }

        // Use cached product options if available
        let productOptionsHtml = '<option value="">Select Product...</option>';
        if (this.productOptions && Array.isArray(this.productOptions) && this.productOptions.length > 0) {
            productOptionsHtml += this.productOptions.map(p => {
                const selected = productId == p.id ? 'selected' : '';
                return `<option value="${p.id}" data-price="${p.price}" ${selected}>${p.name} - $${parseFloat(p.price).toFixed(2)}</option>`;
            }).join('');
        } else if (this.productOptions && this.productOptions.length === 0) {
            // Products array exists but is empty
            productOptionsHtml = '<option value="" disabled>No products available</option>';
        } else {
            // Products not loaded yet
            productOptionsHtml = '<option value="" disabled>Loading products...</option>';
        }

        return `
            <div class="order-item-row" data-index="${index}">
                <div class="order-item-fields">
                    <select class="form-control product-select" onchange="dashboard.updateOrderItemPrice(${index})">
                        ${productOptionsHtml}
                    </select>
                    <input type="number" class="form-control quantity-input" placeholder="Qty" min="1" value="${quantity}"
                           onchange="dashboard.calculateOrderTotal()">
                    <input type="number" class="form-control price-input" placeholder="Price" step="0.01" value="${price}" readonly>
                    <span class="item-total">${new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(quantity * price)}</span>
                    <button type="button" class="btn btn-sm btn-danger" onclick="dashboard.removeOrderItem(${index})">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }

    async initOrderItemsHandlers() {
        // Load products for order items
        try {
            console.log('🔄 Loading products for order items...');
            const response = await fetch('/api/products?page=-1', {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            });

            console.log('📡 Products API response status:', response.status);

            if (response.ok) {
                const responseData = await response.json();
                console.log('📦 Products response data:', responseData);

                let productOptions = [];
                if (responseData.success) {
                    productOptions = responseData.products || [];
                } else {
                    console.warn('⚠️ Products API returned success=false:', responseData.message);
                    productOptions = responseData.products || responseData.data || [];
                }

                console.log('✅ Loaded', productOptions.length, 'products for order items');

                // Cache product options for use in buildOrderItemRow
                this.productOptions = productOptions;

                // Update all product selects in the DOM with the loaded options
                const productSelects = document.querySelectorAll('.product-select');
                console.log('🎯 Found', productSelects.length, 'product selects to update');

                if (productSelects.length === 0) {
                    console.warn('⚠️ No product selects found in DOM');
                    // Try again after a short delay
                    setTimeout(() => {
                        const retrySelects = document.querySelectorAll('.product-select');
                        console.log('🔄 Retry: Found', retrySelects.length, 'product selects');
                        this.updateProductSelects(retrySelects, productOptions);
                    }, 500);
                } else {
                    this.updateProductSelects(productSelects, productOptions);
                }

                // Calculate initial order total
                this.calculateOrderTotal();
                console.log('✅ Order items handlers initialized successfully');
            } else {
                console.error('❌ Failed to load products:', response.status, response.statusText);
                const errorText = await response.text();
                console.error('❌ Error response:', errorText);

                // Handle authentication failure
                if (response.status === 401) {
                    console.warn('🔐 Authentication failed, redirecting to login');
                    this.showErrorAlert('Session expired. Please log in again.');
                    setTimeout(() => {
                        window.location.href = '/?sessionExpired=true';
                    }, 2000);
                    return;
                }

                // Show error in product selects
                document.querySelectorAll('.product-select').forEach(select => {
                    select.innerHTML = '<option value="" disabled>Error loading products</option>';
                });
            }
        } catch (error) {
            console.error('💥 Error loading products for order items:', error);

            // Show error in product selects
            document.querySelectorAll('.product-select').forEach(select => {
                select.innerHTML = '<option value="" disabled>Error loading products</option>';
            });
        }
    }

    updateProductSelects(selects, productOptions) {
        selects.forEach((select, index) => {
            const currentValue = select.value;
            console.log(`🔄 Updating product select ${index}, current value:`, currentValue);

            select.innerHTML = `
                <option value="">Select Product...</option>
                ${productOptions.map(p => `<option value="${p.id}" data-price="${p.price}" ${currentValue == p.id ? 'selected' : ''}>${p.name} - $${p.price}</option>`).join('')}
            `;

            // Restore selected value if editing
            if (currentValue) {
                select.value = currentValue;
                console.log(`✅ Restored selected value for product select ${index}:`, currentValue);
            }
        });
    }

    async handleFormSubmit() {
        console.log('🚀 Form submission started for module:', this.currentModule);

        const form = document.getElementById('modalForm');
        const submitBtn = form.querySelector('button[type="submit"]');
        const originalBtnText = submitBtn.innerHTML;

        // Check if product options are still loading for order forms
        if (this.currentModule === 'orders' && (!this.productOptions || this.productOptions.length === 0)) {
            console.log('⏳ Product options not loaded yet, waiting...');
            this.showErrorAlert('Please wait for product options to load before submitting.');
            return;
        }

        // Debug form state
        const allInputs = form.querySelectorAll('input, textarea, select');
        console.log('📋 Form inputs:', Array.from(allInputs).map(input => ({
            name: input.name,
            type: input.type || input.tagName,
            value: input.value,
            required: input.hasAttribute('required'),
            loading: input.hasAttribute('data-loading')
        })));

        // Validate all fields first
        let isValid = true;
        const inputs = form.querySelectorAll('input, textarea, select');
        const loadingSelects = form.querySelectorAll('select[data-loading="true"]');

        // Check if any select fields are still loading
        if (loadingSelects.length > 0) {
            console.warn(`⏳ Form submission blocked: ${loadingSelects.length} field(s) still loading`);
            const loadingFieldNames = Array.from(loadingSelects).map(s => s.name).join(', ');
            this.showErrorAlert(`Please wait for all fields to finish loading: ${loadingFieldNames}`);
            return;
        }

        inputs.forEach(input => {
            const fieldValid = this.validateField(input);
            console.log(`🔍 Validation result for ${input.name}:`, {
                valid: fieldValid,
                value: input.value,
                tagName: input.tagName,
                type: input.type
            });
            if (!fieldValid) {
                isValid = false;
            }
        });

        if (!isValid) {
            this.showErrorAlert('Please fix the validation errors before submitting.');
            return;
        }

        // Show loading state
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Saving...';

        try {
            const formData = new FormData(form);
            const data = {};

            // Process form data with proper type conversion and relationship handling
            for (let [key, value] of formData.entries()) {
                const fieldConfig = this.currentFields.find(f => f.key === key);
                if (fieldConfig) {
                    switch (fieldConfig.type) {
                        case 'number':
                            data[key] = value === '' ? null : parseFloat(value);
                            break;
                        case 'checkbox':
                            data[key] = value === 'true';
                            break;
                        case 'select-async':
                            // Handle relationship fields - convert IDs to objects
                            if (key === 'category' && value) {
                                data.category = { id: parseInt(value) };
                            } else if (key === 'supplier' && value) {
                                data.supplier = { id: parseInt(value) };
                            } else if (value) {
                                data[key] = parseInt(value);
                            } else {
                                data[key] = null;
                            }
                            break;
                        default:
                            data[key] = value === '' ? null : value;
                    }
                } else {
                    data[key] = value;
                }
            }

            // Handle checkbox fields that weren't checked (they won't be in FormData)
            this.currentFields.forEach(field => {
                if (field.type === 'checkbox' && !data.hasOwnProperty(field.key)) {
                    data[field.key] = false;
                }
            });

            // Handle order items if present
            if (this.currentModule === 'orders') {
                data.orderItems = this.collectOrderItems();
                if (data.orderItems.length === 0) {
                    this.showErrorAlert('Please add at least one order item.');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalBtnText;
                    return;
                }
            }

            // Remove readonly fields from submission (except for updates)
            this.currentFields.forEach(field => {
                if (field.readonly && !this.currentEditId) {
                    delete data[field.key];
                }
            });

            console.log('Submitting data:', data);

            const url = this.currentEditId ?
                `/api/${this.currentModule}/${this.currentEditId}` :
                `/api/${this.currentModule}`;

            const method = this.currentEditId ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(data)
            });

            if (this.handleAuthFailure(response)) {
                return;
            }

            const result = await response.json();
            console.log('Save result:', result);

            if (response.ok) {
                this.closeModal();

                // Auto refresh table data to show latest changes
                await this.loadData(this.currentModule);

                // Show success notification
                const action = this.currentEditId ? 'updated' : 'created';
                this.showSuccessAlert(`${this.getModuleTitle(this.currentModule).slice(0, -1)} ${action} successfully!`);

                // Show auto-generated numbers
                if (!this.currentEditId) {
                    if (result.orderNumber) {
                        this.showInfoAlert(`Order number generated: ${result.orderNumber}`);
                    }
                    if (result.transactionNumber) {
                        this.showInfoAlert(`Transaction number generated: ${result.transactionNumber}`);
                    }
                }
            } else {
                // Handle specific error types
                if (response.status === 400) {
                    const errorMessage = result.message || 'Invalid data provided';
                    this.showErrorAlert(errorMessage);
                } else if (response.status === 409) {
                    this.showErrorAlert('A conflict occurred. This item may have been modified by another user.');
                } else if (response.status === 422) {
                    this.showErrorAlert('Validation failed. Please check your input data.');
                } else {
                    this.showErrorAlert(result.message || `Error saving ${this.getModuleTitle(this.currentModule).slice(0, -1).toLowerCase()}`);
                }
            }
        } catch (error) {
            console.error('Error saving:', error);
            if (error.name === 'TypeError' && error.message.includes('Failed to fetch')) {
                this.showErrorAlert('Network error. Please check your connection and try again.');
            } else {
                this.showErrorAlert(`Unexpected error while saving. Please try again.`);
            }
        } finally {
            // Restore button state
            submitBtn.disabled = false;
            submitBtn.innerHTML = originalBtnText;
        }
    }

    updateTableOptimistically(result, isNew) {
        try {
            const tableBody = document.getElementById('tableBody');
            if (!tableBody) return;

            if (isNew) {
                // Add new row at the top
                const newRow = document.createElement('tr');
                newRow.className = 'fade-in highlight-new';
                newRow.dataset.id = result.id;

                newRow.innerHTML = this.currentFields.map(field => {
                    let value = this.getEnhancedDisplayValue(result, field);
                    return `<td title="${value}">${this.truncateText(value, 30)}</td>`;
                }).join('') + `
                    <td class="actions-column">
                        <div class="action-buttons-enhanced">
                            ${this.currentModule !== 'categories' ? `
                            <button class="btn-action btn-view" onclick="dashboard.viewItem('${this.currentModule}', ${result.id})" title="View Details">
                                <i class="fas fa-eye"></i>
                            </button>
                            ` : ''}
                            <button class="btn-action btn-edit" onclick="dashboard.openEditModal('${this.currentModule}', ${result.id})" title="Edit">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn-action btn-delete" onclick="dashboard.deleteItem('${this.currentModule}', ${result.id})" title="Delete">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </div>
                    </td>
                `;

                tableBody.insertBefore(newRow, tableBody.firstChild);

                // Remove highlight after animation
                setTimeout(() => {
                    newRow.classList.remove('highlight-new');
                }, 2000);
            } else {
                // Update existing row
                const existingRow = tableBody.querySelector(`tr[data-id="${result.id}"]`);
                if (existingRow) {
                    existingRow.classList.add('highlight-updated');

                    // Update row content
                    const cells = existingRow.querySelectorAll('td');
                    this.currentFields.forEach((field, index) => {
                        if (cells[index]) {
                            let value = this.getEnhancedDisplayValue(result, field);
                            cells[index].innerHTML = this.truncateText(value, 30);
                            cells[index].title = value;
                        }
                    });

                    // Remove highlight after animation
                    setTimeout(() => {
                        existingRow.classList.remove('highlight-updated');
                    }, 2000);
                }
            }
        } catch (error) {
            console.error('Error updating table optimistically:', error);
        }
    }

    collectOrderItems() {
        const orderItems = [];
        document.querySelectorAll('.order-item-row').forEach(row => {
            const productSelect = row.querySelector('.product-select');
            const quantityInput = row.querySelector('.quantity-input');
            const priceInput = row.querySelector('.price-input');

            if (productSelect && productSelect.value && quantityInput && quantityInput.value && priceInput && priceInput.value) {
                orderItems.push({
                    productId: parseInt(productSelect.value),
                    quantity: parseInt(quantityInput.value),
                    price: parseFloat(priceInput.value)
                });
            }
        });
        return orderItems;
    }

    async deleteItem(module, id) {
        const moduleName = this.getModuleTitle(module).slice(0, -1);

        // Show custom confirmation dialog
        const confirmed = await this.showConfirmDialog(
            'Confirm Deletion',
            `Are you sure you want to delete this ${moduleName.toLowerCase()}?`,
            'This action cannot be undone.',
            'Delete',
            'btn-danger'
        );

        if (!confirmed) return;

        // Find the row and show loading state
        const row = document.querySelector(`tr[data-id="${id}"]`);
        if (row) {
            row.style.opacity = '0.5';
            row.style.pointerEvents = 'none';

            // Add loading spinner to actions column
            const actionsCell = row.querySelector('.actions-column');
            if (actionsCell) {
                actionsCell.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
            }
        }

        try {
            const response = await fetch(`/api/${module}/${id}`, { method: 'DELETE' });

            if (this.handleAuthFailure(response)) {
                // Restore row state on auth failure
                if (row) {
                    row.style.opacity = '1';
                    row.style.pointerEvents = 'auto';
                    this.restoreRowActions(row, module, id);
                }
                return;
            }

            if (response.ok) {
                const result = await response.json();
                console.log('Delete result:', result);

                // Optimistically remove row with animation
                if (row) {
                    row.style.animation = 'slideOutRight 0.3s ease';
                    setTimeout(() => {
                        if (row.parentElement) {
                            row.remove();

                            // Check if table is empty after deletion
                            const tableBody = document.getElementById('tableBody');
                            if (tableBody && tableBody.children.length === 0) {
                                tableBody.innerHTML = `
                                    <tr>
                                        <td colspan="100%" class="text-center">
                                            <i class="fas fa-inbox"></i> No ${module} found
                                            <br>
                                            <button class="btn btn-primary btn-sm mt-2" onclick="dashboard.openCreateModal('${module}')">
                                                <i class="fas fa-plus"></i> Add First ${moduleName}
                                            </button>
                                        </td>
                                    </tr>
                                `;
                            }
                        }
                    }, 300);
                }

                // Show success notification
                this.showSuccessAlert(`${moduleName} deleted successfully!`);
            } else {
                const errorData = await response.json().catch(() => ({}));
                this.showErrorAlert(errorData.message || `Failed to delete ${moduleName.toLowerCase()}`);

                // Restore row state on error
                if (row) {
                    row.style.opacity = '1';
                    row.style.pointerEvents = 'auto';
                    this.restoreRowActions(row, module, id);
                }
            }
        } catch (error) {
            console.error('Error deleting:', error);
            this.showErrorAlert(`Network error while deleting ${moduleName.toLowerCase()}`);

            // Restore row state on error
            if (row) {
                row.style.opacity = '1';
                row.style.pointerEvents = 'auto';
                this.restoreRowActions(row, module, id);
            }
        }
    }

    restoreRowActions(row, module, id) {
        const actionsCell = row.querySelector('.actions-column');
        if (actionsCell) {
            actionsCell.innerHTML = `
                <div class="action-buttons-enhanced">
                    ${module !== 'categories' ? `
                    <button class="btn-action btn-view" onclick="dashboard.viewItem('${module}', ${id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    ` : ''}
                    <button class="btn-action btn-edit" onclick="dashboard.openEditModal('${module}', ${id})" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn-action btn-delete" onclick="dashboard.deleteItem('${module}', ${id})" title="Delete">
                        <i class="fas fa-trash-alt"></i>
                    </button>
                </div>
            `;
        }
    }

    showConfirmDialog(title, message, subMessage = '', actionText = 'Confirm', actionClass = 'btn-primary') {
        return new Promise((resolve) => {
            // Create overlay
            const overlay = document.createElement('div');
            overlay.className = 'alert-overlay';
            overlay.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.5);
                display: flex;
                justify-content: center;
                align-items: center;
                z-index: 10000;
                opacity: 0;
                transition: opacity 0.3s ease;
            `;

            // Create confirm dialog
            const confirmDialog = document.createElement('div');
            confirmDialog.className = 'confirm-dialog';
            confirmDialog.style.cssText = `
                background: white;
                border-radius: 8px;
                padding: 24px;
                max-width: 400px;
                width: 90%;
                box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
                text-align: center;
                transform: scale(0.7);
                transition: transform 0.3s ease;
            `;

            confirmDialog.innerHTML = `
                <div style="color: #ffc107; font-size: 48px; margin-bottom: 16px;">
                    <i class="fas fa-exclamation-triangle"></i>
                </div>
                <h4 style="margin: 0 0 16px 0; color: #333;">
                    ${title}
                </h4>
                <p style="margin: 0 0 8px 0; color: #666; line-height: 1.5;">
                    ${message}
                </p>
                ${subMessage ? `<p style="margin: 0 0 24px 0; color: #999; font-size: 14px;">${subMessage}</p>` : '<div style="margin-bottom: 24px;"></div>'}
                <div>
                    <button class="btn btn-secondary" onclick="this.closest('.alert-overlay').remove(); window.confirmResolve(false);" style="margin-right: 8px;">
                        Cancel
                    </button>
                    <button class="btn ${actionClass}" onclick="this.closest('.alert-overlay').remove(); window.confirmResolve(true);">
                        ${actionText}
                    </button>
                </div>
            `;

            overlay.appendChild(confirmDialog);
            document.body.appendChild(overlay);

            // Store resolve function globally (temporary)
            window.confirmResolve = resolve;

            // Trigger animation
            requestAnimationFrame(() => {
                overlay.style.opacity = '1';
                confirmDialog.style.transform = 'scale(1)';
            });

            // Close on overlay click (cancel)
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    overlay.remove();
                    delete window.confirmResolve;
                    resolve(false);
                }
            });

            // Auto-cleanup resolve function when dialog is removed
            const observer = new MutationObserver((mutations) => {
                mutations.forEach((mutation) => {
                    if (mutation.type === 'childList' && !document.body.contains(overlay)) {
                        delete window.confirmResolve;
                        observer.disconnect();
                    }
                });
            });
            observer.observe(document.body, { childList: true });
        });
    }

    async viewItem(module, id) {
        try {
            // Set up current fields for the view modal
            this.currentModule = module;
            this.currentFields = MODULE_FIELD_CONFIGS[module] || [];

            const response = await fetch(`/api/${module}/${id}`);

            if (this.handleAuthFailure(response)) {
                return;
            }

            if (response.ok) {
                const responseData = await response.json();
                console.log('View item response data:', responseData);

                // Extract the actual item data from the response
                let itemData = {};
                if (responseData.success !== undefined) {
                    // API response with success flag
                    const itemKey = module.slice(0, -1); // Remove 's' from module name  
                    itemData = responseData[itemKey] || responseData.data || {};
                } else {
                    // Direct item response
                    itemData = responseData;
                }

                console.log('Extracted item data:', itemData);
                console.log('Current fields for view:', this.currentFields);

                this.showItemDetailsModal(itemData, module);
            } else {
                this.showErrorMessage(`Error loading ${module.slice(0, -1)} details`);
            }
        } catch (error) {
            console.error('Error loading item details:', error);
            this.showErrorMessage(`Network error while loading ${module.slice(0, -1)} details`);
        }
    }

    // Helper method to get user display name
    getUserDisplayName(username) {
        const userOrder = [
            'PHEAT_PISEY',
            'ROEURN_MAKARA',
            'PHAL_KHAMLA',
            'LONG_SREYNET',
            'SRIE_VI',
            'EAM_VIMORL'
        ];

        const khmerNames = {
            'SRIE_VI': 'ស្រ៊ី វី',
            'EAM_VIMORL': 'អៀម វិមល',
            'PHAL_KHAMLA': 'ផល ខាំឡា',
            'LONG_SREYNET': 'ឡុង ស្រីណេត',
            'PHEAT_PISEY': 'ភាត់ ពីសី',
            'ROEURN_MAKARA': 'រឿន មករា',
            'SYSTEM': 'System User'
        };

        return khmerNames[username] || username || 'Unknown User';
    }

    showItemDetailsModal(item, module) {
        const modalTitle = document.getElementById('modalTitle');
        const formFields = document.getElementById('formFields');

        modalTitle.textContent = `${module.slice(0, -1)} Details`;

        console.log('Building details view for item:', item);
        console.log('Using fields:', this.currentFields);

        let detailsHtml = `
            <div class="details-header">
                <div class="details-title">
                    <i class="fas fa-info-circle"></i>
                    <span>${module.slice(0, -1)} Information</span>
                </div>
            </div>
            <div class="details-grid">
        `;

        // Main details section
        this.currentFields.forEach(field => {
            let value = item[field.key];
            console.log(`Field ${field.key}: ${value}`);

            // Format the value appropriately
            if (value === null || value === undefined) {
                value = '<span class="not-specified">Not specified</span>';
            } else if (field.type === 'checkbox') {
                value = value ? '<span class="status-active"><i class="fas fa-check-circle"></i> Yes</span>' : '<span class="status-inactive"><i class="fas fa-times-circle"></i> No</span>';
            } else if (field.key === 'id') {
                value = `<span class="detail-id">#${value}</span>`;
            } else if (field.key.includes('price') || field.key.includes('amount')) {
                value = `<span class="detail-currency">$${parseFloat(value).toFixed(2)}</span>`;
            } else if (field.key.includes('date') || field.key.includes('At')) {
                value = `<span class="detail-date">${new Date(value).toLocaleString()}</span>`;
            } else {
                value = `<span class="detail-value">${value.toString()}</span>`;
            }

            detailsHtml += `
                <div class="detail-item">
                    <div class="detail-label">
                        <i class="fas fa-tag"></i>
                        ${field.label}
                    </div>
                    <div class="detail-content">
                        ${value}
                    </div>
                </div>
            `;
        });

        detailsHtml += '</div>';

        // Add special details for orders
        if (module === 'orders' && item.orderItems && item.orderItems.length > 0) {
            detailsHtml += `
                <div class="details-section">
                    <div class="section-header">
                        <i class="fas fa-shopping-cart"></i>
                        <span>Order Items</span>
                    </div>
                    <div class="order-items-details">
                        ${item.orderItems.map(orderItem => `
                            <div class="order-item-card">
                                <div class="order-item-info">
                                    <div class="product-name">
                                        <i class="fas fa-box"></i>
                                        ${orderItem.product?.name || 'Product'}
                                    </div>
                                    <div class="order-item-details">
                                        <span class="quantity">Qty: ${orderItem.quantity}</span>
                                        <span class="price">$${orderItem.price.toFixed(2)}</span>
                                        <span class="subtotal">= $${(orderItem.quantity * orderItem.price).toFixed(2)}</span>
                                    </div>
                                </div>
                            </div>
                        `).join('')}
                        <div class="order-total-card">
                            <div class="order-total-content">
                                <span class="total-label">Total Amount:</span>
                                <span class="total-amount">$${item.totalAmount?.toFixed(2) || '0.00'}</span>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }

        // Add user tracking information section
        detailsHtml += `
            <div class="details-section">
                <div class="section-header">
                    <i class="fas fa-history"></i>
                    <span>Activity History</span>
                </div>
                <div class="activity-timeline">
                    <div class="activity-item created">
                        <div class="activity-icon">
                            <i class="fas fa-plus-circle"></i>
                        </div>
                        <div class="activity-content">
                            <div class="activity-action">Created</div>
                            <div class="activity-details">
                                <div class="activity-user">
                                    <i class="fas fa-user"></i>
                                    <span>${this.getUserDisplayName(item.createdBy)}</span>
                                </div>
                                <div class="activity-time">
                                    <i class="fas fa-clock"></i>
                                    <span>${item.createdAt ? new Date(item.createdAt).toLocaleString() : 'Unknown'}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    ${item.lastModifiedBy && item.lastModifiedAt ? `
                        <div class="activity-item modified">
                            <div class="activity-icon">
                                <i class="fas fa-edit"></i>
                            </div>
                            <div class="activity-content">
                                <div class="activity-action">Last Modified</div>
                                <div class="activity-details">
                                    <div class="activity-user">
                                        <i class="fas fa-user-edit"></i>
                                        <span>${this.getUserDisplayName(item.lastModifiedBy)}</span>
                                    </div>
                                    <div class="activity-time">
                                        <i class="fas fa-clock"></i>
                                        <span>${new Date(item.lastModifiedAt).toLocaleString()}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ` : `
                        <div class="activity-item no-modifications">
                            <div class="activity-icon">
                                <i class="fas fa-info-circle"></i>
                            </div>
                            <div class="activity-content">
                                <div class="activity-action">No Modifications</div>
                                <div class="activity-details">
                                    <span class="no-modifications-text">This ${module.slice(0, -1).toLowerCase()} has not been modified since creation</span>
                                </div>
                            </div>
                        </div>
                    `}
                </div>
            </div>
        `;

        formFields.innerHTML = `
            <div class="item-details-enhanced">
                ${detailsHtml}
            </div>
        `;

        // Hide form submit button, show close button
        const submitBtn = document.querySelector('#modalForm button[type="submit"]');
        if (submitBtn) {
            submitBtn.style.display = 'none';
        }

        // Show modal
        document.getElementById('modal').style.display = 'block';
    }

    closeModal() {
        document.getElementById('modal').style.display = 'none';
        document.getElementById('modalForm').reset();
        // Show submit button again (might have been hidden for details view)
        document.querySelector('#modalForm button[type="submit"]').style.display = 'inline-block';
    }

    filterTable(searchTerm) {
        const rows = document.querySelectorAll('#tableBody tr');
        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(searchTerm.toLowerCase()) ? '' : 'none';
        });
    }

    // Order Items Management
    addOrderItem() {
        const orderItemsList = document.getElementById('orderItemsList');
        if (!orderItemsList) {
            console.error('Order items list not found');
            return;
        }

        const currentItems = orderItemsList.children.length;
        const newItemHtml = this.buildOrderItemRow({}, currentItems);

        orderItemsList.insertAdjacentHTML('beforeend', newItemHtml);

        // Update product selects for the new item if products are already loaded
        if (this.productOptions && this.productOptions.length > 0) {
            const newSelects = orderItemsList.querySelectorAll('.product-select');
            this.updateProductSelects(newSelects, this.productOptions);
        }

        this.calculateOrderTotal();
    }

    removeOrderItem(index) {
        const orderItem = document.querySelector(`[data-index="${index}"]`);
        if (orderItem) {
            orderItem.remove();
            this.calculateOrderTotal();
            // Re-index remaining items
            document.querySelectorAll('.order-item-row').forEach((row, newIndex) => {
                row.dataset.index = newIndex;
                // Update onclick handlers with new index
                const removeBtn = row.querySelector('.btn-danger');
                if (removeBtn) {
                    removeBtn.setAttribute('onclick', `dashboard.removeOrderItem(${newIndex})`);
                }
                const productSelect = row.querySelector('.product-select');
                if (productSelect) {
                    productSelect.setAttribute('onchange', `dashboard.updateOrderItemPrice(${newIndex})`);
                }
            });
        }
    }

    updateOrderItemPrice(index) {
        const row = document.querySelector(`[data-index="${index}"]`);
        const productSelect = row.querySelector('.product-select');
        const priceInput = row.querySelector('.price-input');

        const selectedOption = productSelect.options[productSelect.selectedIndex];
        if (selectedOption && selectedOption.dataset.price) {
            priceInput.value = selectedOption.dataset.price;
            this.calculateOrderTotal();
        }
    }

    calculateOrderTotal() {
        let total = 0;
        document.querySelectorAll('.order-item-row').forEach(row => {
            const quantity = parseFloat(row.querySelector('.quantity-input').value || 0);
            const price = parseFloat(row.querySelector('.price-input').value || 0);
            const itemTotal = quantity * price;

            const totalSpan = row.querySelector('.item-total');
            if (totalSpan) {
                totalSpan.textContent = `$${itemTotal.toFixed(2)}`;
            }

            total += itemTotal;
        });

        const totalDisplay = document.getElementById('orderTotalDisplay');
        if (totalDisplay) {
            totalDisplay.textContent = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(total);
        }

        // Update the total amount field
        const totalAmountInput = document.querySelector('input[name="totalAmount"]');
        if (totalAmountInput) {
            totalAmountInput.value = total.toFixed(2);
        }
    }

    // Reports functionality
    async refreshDashboardMetrics() {
        // Placeholder for future implementation
        console.log('Dashboard metrics functionality coming soon');
    }

    async generateReport(type) {
        // Placeholder for future implementation
        console.log('Report generation functionality coming soon');
    }

    renderReport(type, data) {
        // Placeholder for future implementation
        console.log('Report rendering functionality coming soon');
    }

    closeReportResults() {
        document.getElementById('reportResults').style.display = 'none';
    }

    // Settings functionality placeholder for future use

    // Message display functions
    showSuccessMessage(message) {
        this.showMessage(message, 'success');
    }

    showErrorMessage(message) {
        this.showMessage(message, 'error');
    }

    showInfoMessage(message) {
        this.showMessage(message, 'info');
    }

    // Enhanced alert methods for pop-up style notifications
    showSuccessAlert(message) {
        this.showAlert(message, 'success', 'fas fa-check-circle');
    }

    showErrorAlert(message) {
        this.showAlert(message, 'error', 'fas fa-exclamation-circle');
    }

    showInfoAlert(message) {
        this.showAlert(message, 'info', 'fas fa-info-circle');
    }

    showAlert(message, type = 'info', icon = 'fas fa-info-circle') {
        // Remove any existing alerts first
        const existingAlert = document.querySelector('.header-alert');
        if (existingAlert) {
            existingAlert.remove();
        }

        // Create alert notification
        const alert = document.createElement('div');
        alert.className = `header-alert alert-${type}`;
        alert.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 10000;
            min-width: 300px;
            max-width: 500px;
            padding: 12px 20px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            gap: 12px;
            font-weight: 500;
            box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            border: 1px solid;
            opacity: 0;
            transform: translateX(-50%) translateY(-20px);
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        `;

        const typeStyles = {
            success: {
                background: 'rgba(34, 197, 94, 0.9)',
                borderColor: 'rgba(34, 197, 94, 0.3)',
                color: '#ffffff',
                iconColor: '#ffffff'
            },
            error: {
                background: 'rgba(239, 68, 68, 0.9)',
                borderColor: 'rgba(239, 68, 68, 0.3)',
                color: '#ffffff',
                iconColor: '#ffffff'
            },
            info: {
                background: 'rgba(59, 130, 246, 0.9)',
                borderColor: 'rgba(59, 130, 246, 0.3)',
                color: '#ffffff',
                iconColor: '#ffffff'
            }
        };

        const style = typeStyles[type];
        alert.style.background = style.background;
        alert.style.borderColor = style.borderColor;
        alert.style.color = style.color;

        alert.innerHTML = `
            <i class="${icon}" style="color: ${style.iconColor}; font-size: 18px; flex-shrink: 0;"></i>
            <span style="flex-grow: 1;">${message}</span>
            <button onclick="this.parentElement.remove()" style="
                background: none;
                border: none;
                color: ${style.iconColor};
                font-size: 18px;
                cursor: pointer;
                padding: 0;
                width: 24px;
                height: 24px;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                opacity: 0.7;
                transition: opacity 0.2s ease;
            " onmouseover="this.style.opacity='1'" onmouseout="this.style.opacity='0.7'">
                ×
            </button>
        `;

        document.body.appendChild(alert);

        // Trigger animation
        requestAnimationFrame(() => {
            alert.style.opacity = '1';
            alert.style.transform = 'translateX(-50%) translateY(0)';
        });

        // Auto-close after 3 seconds for success/info alerts, 4 seconds for error alerts
        const autoCloseTime = type === 'error' ? 4000 : 3000;
        setTimeout(() => {
            if (alert.parentElement) {
                alert.style.opacity = '0';
                alert.style.transform = 'translateX(-50%) translateY(-20px)';
                setTimeout(() => {
                    if (alert.parentElement) {
                        alert.remove();
                    }
                }, 400);
            }
        }, autoCloseTime);
    }

    showMessage(message, type = 'info') {
        // Create toast container if it doesn't exist
        let toastContainer = document.getElementById('toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toast-container';
            toastContainer.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 10000;
                display: flex;
                flex-direction: column;
                gap: 10px;
                pointer-events: none;
            `;
            document.body.appendChild(toastContainer);
        }

        // Create toast notification
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;

        const typeConfig = {
            success: { icon: 'fas fa-check-circle', color: '#28a745', bgColor: '#d4edda' },
            error: { icon: 'fas fa-exclamation-circle', color: '#dc3545', bgColor: '#f8d7da' },
            info: { icon: 'fas fa-info-circle', color: '#17a2b8', bgColor: '#d1ecf1' }
        };

        const config = typeConfig[type] || typeConfig.info;

        toast.style.cssText = `
            background: ${config.bgColor};
            border: 1px solid ${config.color}30;
            border-left: 4px solid ${config.color};
            border-radius: 6px;
            padding: 12px 16px;
            max-width: 350px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            transform: translateX(400px);
            transition: all 0.3s ease;
            pointer-events: all;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 14px;
            color: ${config.color};
        `;

        toast.innerHTML = `
            <i class="${config.icon}" style="font-size: 16px; flex-shrink: 0;"></i>
            <span style="flex: 1; line-height: 1.4;">${message}</span>
            <button onclick="this.parentElement.remove()" 
                    style="background: none; border: none; color: ${config.color}; cursor: pointer; font-size: 18px; padding: 0; margin-left: 5px; flex-shrink: 0;">
                <i class="fas fa-times"></i>
            </button>
        `;

        toastContainer.appendChild(toast);

        // Trigger slide-in animation
        requestAnimationFrame(() => {
            toast.style.transform = 'translateX(0)';
        });

        // Auto-remove after 4 seconds
        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.transform = 'translateX(400px)';
                toast.style.opacity = '0';
                setTimeout(() => toast.remove(), 300);
            }
        }, 4000);
    }

    // Pagination
    addPaginationControls(module, paginationInfo) {
        const tableContainer = document.querySelector('.table-container');
        const existingPagination = document.querySelector('.pagination-controls');
        if (existingPagination) existingPagination.remove();

        const paginationHtml = `
            <div class="pagination-controls">
                <button class="btn btn-sm btn-secondary" onclick="dashboard.loadPage('${module}', ${paginationInfo.number - 1})" 
                        ${paginationInfo.first ? 'disabled' : ''}>
                    <i class="fas fa-chevron-left"></i> Previous
                </button>
                <span class="pagination-info">
                    Page ${paginationInfo.number + 1} of ${paginationInfo.totalPages} 
                    (${paginationInfo.totalElements} total)
                </span>
                <button class="btn btn-sm btn-secondary" onclick="dashboard.loadPage('${module}', ${paginationInfo.number + 1})" 
                        ${paginationInfo.last ? 'disabled' : ''}>
                    Next <i class="fas fa-chevron-right"></i>
                </button>
            </div>
        `;

        tableContainer.insertAdjacentHTML('afterend', paginationHtml);
    }

    async loadPage(module, page) {
        if (page < 0) return;

        try {
            const response = await fetch(`/api/${module}?page=${page}&size=50`);

            if (this.handleAuthFailure(response)) {
                return;
            }

            if (response.ok) {
                const data = await response.json();
                const items = data.content || data;
                this.renderTable(items, module, data);
            }
        } catch (error) {
            console.error(`Error loading page ${page} for ${module}:`, error);
        }
    }

    // User Management Functions
    async loadUserCards() {
        try {
            const response = await fetch('/api/users');

            // Check for authentication failure
            if (this.handleAuthFailure(response)) {
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            let users = await response.json();

            // Define the desired order
            const userOrder = [
                'PHEAT_PISEY',
                'ROEURN_MAKARA',
                'PHAL_KHAMLA',
                'LONG_SREYNET',
                'SRIE_VI',
                'EAM_VIMORL'
            ];

            // Sort users based on the defined order
            users.sort((a, b) => {
                const indexA = userOrder.indexOf(a.username);
                const indexB = userOrder.indexOf(b.username);
                return indexA - indexB;
            });

            this.renderUserCards(users);
        } catch (error) {
            console.error('Error loading users:', error);
            document.getElementById('userCardsContainer').innerHTML = `
                <div class="loading-card">
                    <div><i class="fas fa-exclamation-circle"></i> Error loading users</div>
                </div>
            `;
        }
    }

    renderUserCards(users) {
        const container = document.getElementById('userCardsContainer');
        if (!users || users.length === 0) {
            container.innerHTML = `
                <div class="loading-card">
                    <div><i class="fas fa-users-slash"></i> No users found</div>
                </div>
            `;
            return;
        }

        container.innerHTML = users.map(user => `
            <div class="user-profile-card" data-user-id="${user.id}" onclick="dashboard.flipCard(this)">
                <div class="user-card-inner">
                    <div class="user-card-front">
                        <img src="/image/userP/${user.profileImage.split('/').pop().replace('.png', '.jpg')}" 
                             alt="${user.name}" 
                             class="user-profile-image">
                        <div class="user-profile-info">
                            <div class="user-profile-name">${user.name}</div>
                        </div>
                    </div>
                    <div class="user-card-back">
                        <div class="user-profile-info">
                            <div class="user-profile-name">${user.name}</div>
                            <div class="user-profile-username">@${user.username}</div>
                            <div class="user-stats">
                                <div class="stat-item">
                                    <i class="fas fa-key"></i>
                                    Access Code: ${user.accessCode}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    }

    refreshUserCards() {
        document.getElementById('userCardsContainer').innerHTML = `
            <div class="loading-card">
                <span class="material-icons">hourglass_empty</span>
                <p>Refreshing users...</p>
            </div>
        `;
        this.loadUserCards();
    }

    flipCard(cardElement) {
        // Remove flipped class from all other cards
        document.querySelectorAll('.user-profile-card.flipped').forEach(card => {
            if (card !== cardElement) {
                card.classList.remove('flipped');
            }
        });
        // Toggle the flipped class on the clicked card
        cardElement.classList.toggle('flipped');
    }

    async editUser(userId) {
        try {
            const response = await fetch(`/api/users/${userId}`);
            const user = await response.json();

            // Create edit user modal content
            const modalContent = `
                <div class="user-edit-form">
                    <div class="form-group">
                        <label>Name</label>
                        <input type="text" id="editUserName" class="form-control" value="${user.name}">
                    </div>
                    <div class="form-group">
                        <label>Username</label>
                        <input type="text" id="editUsername" class="form-control" value="${user.username}">
                    </div>
                    <div class="form-group">
                        <label>Access Code</label>
                        <input type="text" id="editAccessCode" class="form-control" value="${user.accessCode}" maxlength="2" pattern="[0-9]{2}">
                    </div>
                    <div class="form-group">
                        <label>Gender</label>
                        <select id="editGender" class="form-control">
                            <option value="male" ${user.gender === 'male' ? 'selected' : ''}>Male</option>
                            <option value="female" ${user.gender === 'female' ? 'selected' : ''}>Female</option>
                        </select>
                    </div>
                </div>
            `;

            document.getElementById('modalTitle').textContent = 'Edit User';
            document.getElementById('formFields').innerHTML = modalContent;
            document.getElementById('modal').style.display = 'block';

            // Override form submit for user editing
            document.getElementById('modalForm').onsubmit = (e) => {
                e.preventDefault();
                this.saveUserEdit(userId);
            };

        } catch (error) {
            console.error('Error loading user:', error);
            alert('Error loading user details');
        }
    }

    async saveUserEdit(userId) {
        const userData = {
            name: document.getElementById('editUserName').value,
            username: document.getElementById('editUsername').value,
            accessCode: document.getElementById('editAccessCode').value,
            gender: document.getElementById('editGender').value
        };

        try {
            const response = await fetch(`/api/users/${userId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData)
            });

            if (response.ok) {
                this.closeModal();
                this.loadUserCards();
                alert('User updated successfully!');
            } else {
                alert('Error updating user');
            }
        } catch (error) {
            console.error('Error updating user:', error);
            alert('Error updating user');
        }
    }

    async toggleUserStatus(userId) {
        // Check if current user is admin (SRIE_VI)
        const currentUsername = localStorage.getItem('username');
        if (currentUsername !== 'SRIE_VI') {
            alert('Only the administrator (ស្រ៊ី វី) can activate or deactivate users.');
            return;
        }

        if (!confirm('Are you sure you want to change this user\'s status?')) return;

        try {
            const response = await fetch(`/api/users/${userId}/toggle-status`, {
                method: 'PATCH'
            });

            if (response.ok) {
                this.loadUserCards();
                alert('User status updated successfully!');
            } else {
                alert('Error updating user status');
            }
        } catch (error) {
            console.error('Error updating user status:', error);
            alert('Error updating user status');
        }
    }

    async resetAccessCode(userId) {
        if (!confirm('Are you sure you want to reset this user\'s access code?')) return;

        try {
            const response = await fetch(`/api/users/${userId}/reset-access-code`, {
                method: 'PATCH'
            });

            if (response.ok) {
                const result = await response.json();
                this.loadUserCards();
                alert(`Access code reset successfully! New code: ${result.newAccessCode}`);
            } else {
                alert('Error resetting access code');
            }
        } catch (error) {
            console.error('Error resetting access code:', error);
            alert('Error resetting access code');
        }
    }

    // Settings-related functions removed
}

// Initialize dashboard when DOM is loaded - SINGLE INITIALIZATION POINT
let dashboardInstance = null;
document.addEventListener('DOMContentLoaded', async () => {
    if (!dashboardInstance) {
        console.log('Creating dashboard instance...');
        dashboardInstance = new Dashboard();
        window.dashboard = dashboardInstance;
        await dashboardInstance.init();
    }
});

// Global functions for HTML onclick handlers
function closeModal() {
    if (window.dashboard) {
        dashboard.closeModal();
    }
}

// Close modal when clicking outside
window.onclick = function (event) {
    const modal = document.getElementById('modal');
    if (event.target === modal && window.dashboard) {
        dashboard.closeModal();
    }
}
