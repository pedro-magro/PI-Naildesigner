document.addEventListener('DOMContentLoaded', () => {
            const hamburgerToggle = document.getElementById('hamburger-toggle');
            const offCanvasMenu = document.getElementById('off-canvas-menu');
            const offCanvasClose = document.getElementById('off-canvas-close');
            const menuOverlay = document.getElementById('menu-overlay');

            function openMenu() {
                offCanvasMenu.classList.add('active');
                menuOverlay.classList.add('active');
                document.body.style.overflow = 'hidden'; // Impede rolagem do body quando o menu está aberto
            }

            function closeMenu() {
                offCanvasMenu.classList.remove('active');
                menuOverlay.classList.remove('active');
                document.body.style.overflow = ''; // Restaura a rolagem do body
            }

            hamburgerToggle.addEventListener('click', openMenu);
            offCanvasClose.addEventListener('click', closeMenu);
            menuOverlay.addEventListener('click', closeMenu); // Fecha o menu ao clicar no overlay
        });