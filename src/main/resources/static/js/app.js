document.addEventListener('DOMContentLoaded', () => {
    // 1. Theme Toggle System
    const themeToggleBtn = document.getElementById('theme-toggle-btn');
    const themeIcon = document.getElementById('theme-icon');
    const themeText = document.getElementById('theme-text');
    
    // Check saved theme
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
    updateThemeUI(savedTheme);

    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const currentTheme = document.documentElement.getAttribute('data-theme');
            const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
            document.documentElement.setAttribute('data-theme', newTheme);
            localStorage.setItem('theme', newTheme);
            updateThemeUI(newTheme);
        });
    }

    function updateThemeUI(theme) {
        if (!themeIcon || !themeText) return;
        if (theme === 'dark') {
            themeIcon.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M8 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8M8 0a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 0m0 13a.5.5 0 0 1 .5.5v2a.5.5 0 0 1-1 0v-2A.5.5 0 0 1 8 13m8-5a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2a.5.5 0 0 1 .5.5M3 8a.5.5 0 0 1-.5.5h-2a.5.5 0 0 1 0-1h2A.5.5 0 0 1 3 8m10.657-5.657a.5.5 0 0 1 0 .707l-1.414 1.415a.5.5 0 1 1-.707-.708l1.414-1.414a.5.5 0 0 1 .707 0m-9.193 9.193a.5.5 0 0 1 0 .707L3.05 13.657a.5.5 0 0 1-.707-.707l1.414-1.414a.5.5 0 0 1 .707 0m9.193 2.121a.5.5 0 0 1-.707 0l-1.414-1.414a.5.5 0 0 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .707M4.464 4.465a.5.5 0 0 1-.707 0L2.343 3.05a.5.5 0 1 1 .707-.707l1.414 1.414a.5.5 0 0 1 0 .708"/>
                </svg>`;
            themeText.textContent = 'Light';
        } else {
            themeIcon.innerHTML = `
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                  <path d="M6 .278a.77.77 0 0 1 .08.858 7.2 7.2 0 0 0-.878 3.46c0 4.021 3.278 7.277 7.318 7.277q.792-.001 1.533-.16a.79.79 0 0 1 .81.316.73.73 0 0 1-.031.893A8.35 8.35 0 0 1 8.344 16C3.734 16 0 12.286 0 7.71 0 4.266 2.114 1.312 5.124.06A.75.75 0 0 1 6 .278M4.858 1.311A7.27 7.27 0 0 0 1.025 7.71c0 4.02 3.279 7.276 7.319 7.276a7.28 7.28 0 0 0 5.201-2.176 10.7 10.7 0 0 1-8.69-8.69 10.7 10.7 0 0 1-3.21-3.321"/>
                </svg>`;
            themeText.textContent = 'Dark';
        }
    }


    // 2. Mobile Responsive Hamburguer Navbar Menu
    const burgerMenuBtn = document.getElementById('burger-menu-btn');
    const navMenu = document.getElementById('nav-menu');
    if (burgerMenuBtn && navMenu) {
        burgerMenuBtn.addEventListener('click', () => {
            navMenu.classList.toggle('active');
        });
    }

    // Scroll Navbar Effect
    const navbar = document.getElementById('navbar-container');
    window.addEventListener('scroll', () => {
        if (navbar) {
            if (window.scrollY > 15) {
                navbar.classList.add('scrolled');
            } else {
                navbar.classList.remove('scrolled');
            }
        }
    });


    // 3. Dynamic Slots Builder
    const addSlotBtn = document.getElementById('add-slot-btn');
    const slotsGrid = document.getElementById('slots-grid');
    if (addSlotBtn && slotsGrid) {
        addSlotBtn.addEventListener('click', () => {
            const slotItem = document.createElement('div');
            slotItem.className = 'slot-item';
            slotItem.innerHTML = `
                <select name="slotDays" required class="form-control">
                    <option value="Mon">Monday</option>
                    <option value="Tue">Tuesday</option>
                    <option value="Wed">Wednesday</option>
                    <option value="Thu">Thursday</option>
                    <option value="Fri">Friday</option>
                    <option value="Sat">Saturday</option>
                    <option value="Sun">Sunday</option>
                </select>
                <input type="time" name="slotStarts" required class="form-control" value="09:00">
                <input type="time" name="slotEnds" required class="form-control" value="17:00">
                <button type="button" class="btn btn-secondary btn-sm remove-slot-btn" style="padding: 6px 12px; background: rgba(220,38,38,0.1); color:#dc2626; border-color:transparent;">
                    Remove
                </button>
            `;
            slotsGrid.appendChild(slotItem);
            
            // Attach event to remove button
            slotItem.querySelector('.remove-slot-btn').addEventListener('click', () => {
                slotItem.remove();
            });
        });

        // Attach event to existing remove buttons (if any rendered by thymeleaf)
        document.querySelectorAll('.remove-slot-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.target.closest('.slot-item').remove();
            });
        });
    }


    // 4. Modal Handlers
    window.openModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('active');
        }
    };

    window.closeModal = function(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('active');
        }
    };
    
    // Close modal on clicking overlay
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('active');
            }
        });
    });
});
Commit at 2026-10-02T12:06:02
Commit at 2026-10-04T10:54:33
Commit at 2026-10-02T19:50:38
Commit at 2026-09-25T04:35:13
Commit at 2026-10-02T20:26:15
Commit at 2026-09-30T20:13:06
Commit at 2026-10-01T05:10:46
Commit at 2026-09-25T09:40:09
Commit at 2026-09-29T19:47:07
Commit at 2026-10-02T01:42:21
Commit at 2026-10-04T17:18:10
Commit at 2026-10-02T12:57:37
Commit at 2026-10-04T07:27:49
Commit at 2026-10-05T02:28:31
Commit at 2026-10-04T14:49:49
Commit at 2026-09-29T21:30:56
Commit at 2026-09-25T19:55:53
Commit at 2026-09-27T17:13:43
Commit at 2026-10-04T09:38:02
Commit at 2026-10-05T07:36:11
Commit at 2026-09-28T10:37:31
Commit at 2026-09-26T03:02:07
Commit at 2026-09-25T16:11:22
Commit at 2026-10-02T06:47:43
Commit at 2026-09-28T00:23:44
