/**
 * portal-manager.js — owned by IT25102082 (Blood Bank Manager).
 * Loaded only by templates/portal/manager.html.
 * Put manager-specific UI behaviour here (inventory UX, stock alerts,
 * approve/reject confirmations). Shared behaviour stays in js/app.js.
 */
(function () {
    'use strict';

    function confirmRejectForms() {
        document.querySelectorAll('form[action*="/portal/manager/reject/"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Reject this blood request?')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmRejectForms();
    });
})();
