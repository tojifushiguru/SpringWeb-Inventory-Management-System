// Settings Module
const settings = {
       init() {
              this.bindEvents();
              this.loadSettings();
       },

       bindEvents() {
              // Tab navigation
              document.querySelectorAll('.nav-tab').forEach(tab => {
                     tab.addEventListener('click', () => this.switchTab(tab.dataset.tab));
              });

              // Save settings button
              document.querySelector('#saveSettings').addEventListener('click', () => this.saveAllSettings());

              // Reset settings button
              document.querySelector('#resetSettings').addEventListener('click', () => this.resetToDefaults());
       },

       showLoading() {
              return `
            <div class="loading-container">
                <div class="loading-pulse"></div>
                <div class="loading-text">
                    Loading
                    <div class="loading-dots">
                        <span></span>
                        <span></span>
                        <span></span>
                    </div>
                </div>
            </div>
        `;
       },

       loadSettings() {
              const settingsContent = document.querySelector('#settingsContent');
              // Show loading state first
              settingsContent.innerHTML = this.showLoading();

              // Simulate loading delay (remove this in production)
              setTimeout(() => {
                     settingsContent.innerHTML = `
                <div class="settings-container">
                    <div class="settings-header">
                        <h1 class="settings-title">
                            <i class="material-icons">settings</i>
                            Settings
                        </h1>
                        <div class="settings-actions">
                            <button class="btn btn-primary" id="saveSettings">
                                <i class="material-icons">save</i>
                                Save Changes
                            </button>
                            <button class="btn btn-secondary" id="resetSettings">
                                <i class="material-icons">refresh</i>
                                Reset to Defaults
                            </button>
                        </div>
                    </div>
                
                <div class="settings-update-message">
                    <i class="material-icons">info</i>
                    <div class="message-content">
                        <h3>Settings Update Coming Soon</h3>
                        <p>We're working on exciting new features for the next version. Stay tuned for enhanced configuration options!</p>
                    </div>
                </div>

                <div class="settings-navigation">
                    <div class="nav-tab active" data-tab="general">
                        <i class="material-icons">tune</i>
                        General
                    </div>
                    <div class="nav-tab" data-tab="notifications">
                        <i class="material-icons">notifications</i>
                        Notifications
                    </div>
                    <div class="nav-tab" data-tab="security">
                        <i class="material-icons">security</i>
                        Security
                    </div>
                    <div class="nav-tab" data-tab="appearance">
                        <i class="material-icons">palette</i>
                        Appearance
                    </div>
                </div>

                <div class="settings-content">
                    <!-- General Settings -->
                    <div class="settings-section active" id="generalSettings">
                        <div class="settings-grid">
                            ${this.renderGeneralSettings()}
                        </div>
                    </div>

                    <!-- Notifications Settings -->
                    <div class="settings-section" id="notificationsSettings">
                        <div class="settings-grid">
                            ${this.renderNotificationSettings()}
                        </div>
                    </div>

                    <!-- Security Settings -->
                    <div class="settings-section" id="securitySettings">
                        <div class="settings-grid">
                            ${this.renderSecuritySettings()}
                        </div>
                    </div>

                    <!-- Appearance Settings -->
                    <div class="settings-section" id="appearanceSettings">
                        <div class="settings-grid">
                            ${this.renderAppearanceSettings()}
                        </div>
                    </div>
                </div>

                <!-- User Management Section -->
                <div class="user-management">
                    <div class="section-title">
                        <i class="material-icons">people</i>
                        User Management
                    </div>
                    <div class="user-grid">
                        ${this.renderUserManagement()}
                    </div>
                </div>
            </div>
        `;

                     this.bindEvents();
              },

                     switchTab(tabId) {
                     // Update active tab
                     document.querySelectorAll('.nav-tab').forEach(tab => {
                            tab.classList.toggle('active', tab.dataset.tab === tabId);
                     });

                     // Show corresponding section
                     document.querySelectorAll('.settings-section').forEach(section => {
                            section.classList.toggle('active', section.id === tabId + 'Settings');
                     });
              },

                     renderGeneralSettings() {
                     return `
            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">language</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Language</div>
                        <div class="setting-description">Choose your preferred language</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="en">English</option>
                        <option value="km">ខ្មែរ</option>
                    </select>
                </div>
            </div>

            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">schedule</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Time Zone</div>
                        <div class="setting-description">Set your local time zone</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="Asia/Phnom_Penh">Asia/Phnom Penh (UTC+7)</option>
                        <option value="UTC">UTC</option>
                    </select>
                </div>
            </div>

            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">date_range</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Date Format</div>
                        <div class="setting-description">Choose how dates are displayed</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                        <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                        <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                    </select>
                </div>
            </div>
        `;
              },

                     renderNotificationSettings() {
                     return `
            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">inventory</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Stock Alerts</div>
                        <div class="setting-description">Get notified when inventory is low</div>
                    </div>
                </div>
                <div class="setting-control">
                    <div class="switch-control">
                        <label class="switch">
                            <input type="checkbox" checked>
                            <span class="slider"></span>
                        </label>
                        <span>Enable notifications</span>
                    </div>
                </div>
            </div>

            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">shopping_cart</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Order Updates</div>
                        <div class="setting-description">Receive notifications for new orders</div>
                    </div>
                </div>
                <div class="setting-control">
                    <div class="switch-control">
                        <label class="switch">
                            <input type="checkbox" checked>
                            <span class="slider"></span>
                        </label>
                        <span>Enable notifications</span>
                    </div>
                </div>
            </div>
        `;
              },

                     renderSecuritySettings() {
                     return `
            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">password</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Password Requirements</div>
                        <div class="setting-description">Set minimum password complexity</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="medium">Medium (8+ characters)</option>
                        <option value="strong">Strong (12+ chars with symbols)</option>
                        <option value="very-strong">Very Strong (16+ mixed chars)</option>
                    </select>
                </div>
            </div>

            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">security</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Two-Factor Authentication</div>
                        <div class="setting-description">Add an extra layer of security</div>
                    </div>
                </div>
                <div class="setting-control">
                    <div class="switch-control">
                        <label class="switch">
                            <input type="checkbox">
                            <span class="slider"></span>
                        </label>
                        <span>Enable 2FA</span>
                    </div>
                </div>
            </div>
        `;
              },

                     renderAppearanceSettings() {
                     return `
            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">dark_mode</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Theme</div>
                        <div class="setting-description">Choose your preferred theme</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="dark">Dark Theme</option>
                        <option value="light">Light Theme</option>
                        <option value="system">System Default</option>
                    </select>
                </div>
            </div>

            <div class="setting-card">
                <div class="setting-header">
                    <div class="setting-icon">
                        <i class="material-icons">text_fields</i>
                    </div>
                    <div class="setting-info">
                        <div class="setting-title">Font Size</div>
                        <div class="setting-description">Adjust the text size</div>
                    </div>
                </div>
                <div class="setting-control">
                    <select class="form-control">
                        <option value="small">Small</option>
                        <option value="medium" selected>Medium</option>
                        <option value="large">Large</option>
                    </select>
                </div>
            </div>
        `;
              },

                     showUserManagementLoading() {
                     return `
            <div class="user-grid">
                ${Array(6).fill().map(() => `
                    <div class="user-card skeleton-loader">
                        <div class="skeleton-text" style="width: 60px; height: 60px; border-radius: 50%;"></div>
                        <div class="skeleton-text" style="width: 120px;"></div>
                        <div class="skeleton-text" style="width: 80px;"></div>
                    </div>
                `).join('')}
            </div>
        `;
              },

                     renderUserManagement() {
                            // Show loading state first
                            const userSection = document.querySelector('.user-management .user-grid');
                            if(userSection) {
                                   userSection.innerHTML = this.showUserManagementLoading();
                            }

        // Simulate loading delay (remove in production)
        setTimeout(() => {
       const users = [
              {
                     name: 'ថ្នក់',
                     role: 'Admin',
                     avatar: '/image/profile/admin.jpg'
              },
              {
                     name: 'ថ្នែង វាសនា',
                     role: 'Manager',
                     avatar: '/image/profile/manager.jpg'
              },
              // Add more users as needed
       ];

       return users.map(user => `
            <div class="user-card">
                <img src="${user.avatar}" alt="${user.name}" class="user-avatar">
                <div class="user-info">
                    <div class="user-name">${user.name}</div>
                    <div class="user-role">${user.role}</div>
                </div>
                <div class="user-actions">
                    <button class="btn btn-sm btn-primary">
                        <i class="material-icons">edit</i>
                    </button>
                    <button class="btn btn-sm btn-danger">
                        <i class="material-icons">delete</i>
                    </button>
                </div>
            </div>
        `).join('');
},

saveAllSettings() {
       // Implement settings save logic here
       alert('Settings saved successfully!');
},

resetToDefaults() {
       if (confirm('Are you sure you want to reset all settings to defaults?')) {
              // Implement reset logic here
              alert('Settings have been reset to defaults');
       }
}
};

// Initialize settings module
document.addEventListener('DOMContentLoaded', () => {
       settings.init();
});