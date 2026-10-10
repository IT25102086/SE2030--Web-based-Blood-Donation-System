/**
 * portal-hospital.js — owned by IT25102084 (Hospital Coordinator).
 * Loaded only by templates/portal/hospital.html.
 * Put hospital-specific UI behaviour here (requisition form UX, live
 * availability check, coordination helpers). Shared behaviour stays in js/app.js.
 */
(function () {
    'use strict';

    function confirmDestructiveForms() {
        document.querySelectorAll('form[action*="/portal/hospital/requisition/"][action$="/delete"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Cancel this hospital requisition? This cannot be undone.')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmDestructiveForms();
    });
})();
