/**
 * portal-delivery.js — owned by IT25102081 (Delivery Coordination Officer).
 * Loaded only by templates/portal/delivery.html.
 * Put delivery-specific UI behaviour here (assignment UX, tracking helpers,
 * status confirmations). Shared behaviour stays in js/app.js.
 */
(function () {
    'use strict';

    function confirmCancelForms() {
        document.querySelectorAll('form[action*="/portal/delivery/"][action$="/cancel"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Cancel this delivery order? The request returns to the approved pool.')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmCancelForms();
    });
})();
