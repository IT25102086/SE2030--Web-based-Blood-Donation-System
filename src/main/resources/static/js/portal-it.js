/**
 * portal-it.js — owned by IT25102086 (IT Operations Supervisor).
 * Loaded only by templates/portal/it.html.
 * Put IT-specific UI behaviour here (user-table filters, permission matrix
 * helpers, audit-log UX). Shared behaviour stays in js/app.js.
 */
(function () {
    'use strict';

    function confirmDeleteForms() {
        document.querySelectorAll('form[action*="/portal/it/users/"][action$="/delete"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Permanently remove this user account?')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmDeleteForms();
    });
})();
