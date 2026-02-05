/* ===============================================
   GALAXY - SCROLLSPY NAVIGATION
   =============================================== */

// ================= SCROLLSPY INITIALIZATION =================
function initScrollspy() {
    const sections = document.querySelectorAll('.scroll-section');
    const navItems = document.querySelectorAll('.scrollspy-item');

    if (sections.length === 0 || navItems.length === 0) return;

    // Smooth scroll on click
    navItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href').substring(1);
            const targetSection = document.getElementById(targetId);

            if (targetSection) {
                // Smooth scroll to section
                targetSection.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });

                // Update active state
                updateActiveNav(targetId);
            }
        });
    });

    // Update active state on scroll
    let scrollTimeout;
    window.addEventListener('scroll', function() {
        clearTimeout(scrollTimeout);
        scrollTimeout = setTimeout(function() {
            updateScrollspy();
        }, 50);
    });

    // Initial check
    updateScrollspy();
}

// ================= UPDATE ACTIVE NAV ITEM =================
function updateActiveNav(activeId) {
    const navItems = document.querySelectorAll('.scrollspy-item');
    navItems.forEach(item => {
        const sectionId = item.getAttribute('data-section');
        if (sectionId === activeId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });
}

// ================= UPDATE SCROLLSPY ON SCROLL =================
function updateScrollspy() {
    const sections = document.querySelectorAll('.scroll-section');
    const navItems = document.querySelectorAll('.scrollspy-item');

    if (sections.length === 0) return;

    // Get current scroll position with some offset
    const scrollPosition = window.scrollY + 150;

    let currentSection = null;

    // Find the current section
    sections.forEach(section => {
        const sectionTop = section.offsetTop;
        const sectionHeight = section.offsetHeight;

        if (scrollPosition >= sectionTop && scrollPosition < sectionTop + sectionHeight) {
            currentSection = section.id;
        }
    });

    // If no section found (at top of page), default to first section
    if (!currentSection && sections.length > 0) {
        // Check if we're above all sections
        if (scrollPosition < sections[0].offsetTop) {
            currentSection = sections[0].id;
        } else {
            // We're below all sections, use the last one
            currentSection = sections[sections.length - 1].id;
        }
    }

    // Update active state
    if (currentSection) {
        updateActiveNav(currentSection);
    }
}

// ================= SCROLL TO SECTION =================
function scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section) {
        section.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
        });
        updateActiveNav(sectionId);
    }
}

// ================= INITIALIZE ON DOM READY =================
document.addEventListener('DOMContentLoaded', function() {
    // Small delay to ensure all elements are rendered
    setTimeout(initScrollspy, 100);
});

// Also initialize when window is fully loaded (including images)
window.addEventListener('load', function() {
    initScrollspy();
});
