/**
 * portal-qa.js — owned by IT25102085 (Quality Assurance Officer).
 * Loaded only by templates/portal/qa.html.
 * Put QA-specific UI behaviour here (screening checklist UX, unit filters,
 * approve/reject confirmations). Shared behaviour stays in js/app.js.
 */
(function () {
    'use strict';

    function confirmDiscardForms() {
        document.querySelectorAll('form[action*="/portal/qa/unit/"][action$="/discard"]').forEach(function (form) {
            form.addEventListener('submit', function (e) {
                if (!window.confirm('Decommission this blood unit? This marks it as Discarded.')) {
                    e.preventDefault();
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        confirmDiscardForms();
    });
})();
