/**
 * portal-recipient.js — owned by IT25102090 (Recipient / Patient).
 * Loaded only by templates/portal/recipient.html.
 * Put recipient-specific UI behaviour here (request form UX, status filters,
 * history table helpers). Shared behaviour stays in js/app.js — do NOT copy it here.
 */
(function () {
    'use strict';

    function confirmDestructiveForms() {
        document.querySelectorAll('form[action*="/portal/recipient/request/"][action$="/delete"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Cancel this blood request? This cannot be undone.')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmDestructiveForms();
    });
})();
