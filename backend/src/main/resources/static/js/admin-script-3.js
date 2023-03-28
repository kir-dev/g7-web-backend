let menuItems;
let lastShiftPress = 0;

let searchInput;
let searchItems;
let input;
let results;
let selectedIndex = -1;

function isModalOpen() {
    return document.getElementById('search-modal').style.display !== 'none';
}

function init() {
    document.getElementById('search').addEventListener('keyup', searchListener());
    document.getElementById('search-input').addEventListener('keyup', siteSearchListener());

    searchInput = document.querySelector("#search");

    document.addEventListener("keydown", (event) => {
        const currentTime = new Date().getTime();

        // Double shift
        if (event.keyCode === 16) {
            if (currentTime - lastShiftPress < 300) {
                openSearch();
            }
            lastShiftPress = currentTime;
            event.preventDefault();

            // F2
        } else if (event.keyCode === 113) {
            searchInput.focus();
            searchInput.select();
            event.preventDefault();

            // ESC
        } else if (event.keyCode === 27) {
            if (isModalOpen()) {
                closeSearch();
                event.preventDefault();
            }
        }
    });

    input = document.getElementById('search-input');
    results = Array.from(document.querySelectorAll(".search-result a")).filter(function(result) {
        return result.style.display !== 'none';
    });

    input.addEventListener("keydown", function(event) {
        if (event.keyCode === 38) {
            event.preventDefault();
            selectedIndex = selectedIndex > 0 ? selectedIndex - 1 : results.length - 1;
            selectResult(selectedIndex);

        } else if (event.keyCode === 40 || event.keyCode === 9) {
            event.preventDefault();
            selectedIndex = selectedIndex < results.length - 1 ? selectedIndex + 1 : 0;
            selectResult(selectedIndex);

        } else if (event.keyCode === 13 && selectedIndex > -1 && isModalOpen()) {
            event.preventDefault();
            window.location.href = results[selectedIndex].href;
        }
    });
}

function selectResult(index) {
    results.forEach(function(result) {
        result.classList.remove("selected");
    });
    results[index].classList.add("selected");
}

function searchListener() {
    menuItems = document.querySelectorAll("#menu-results a");

    return function () {
        const searchString = this.value.toLowerCase();

        menuItems.forEach(function (menuItem) {
            let menuItemText = menuItem.textContent.toLowerCase();
            console.log(menuItem.getAttribute('data-search'));
            if (menuItem.getAttribute('data-search')) {
                menuItemText = menuItem.getAttribute('data-search').toLowerCase();
            }

            if (menuItemText.includes(searchString)) {
                menuItem.style.display = 'block';
            } else {
                menuItem.style.display = 'none';
            }
        });
    };
}

function searchFavorite() {
    document.getElementById('search').value = '';
    menuItems.forEach(function (menuItem) {
        const status = menuItem.className;

        if (status.includes('favorite')) {
            menuItem.style.display = 'block';
        } else {
            menuItem.style.display = 'none';
        }
    });
}

function searchEverything() {
    menuItems.forEach(function (menuItem) {
        menuItem.style.display = 'block';
    });
}

function clearFilter() {
    document.getElementById('search').value = '';
    searchEverything();
}

function closeSearch() {
    document.getElementById('background-fader').style.display = 'none';
    document.getElementById('search-modal').style.display = 'none';
    let searchInput =  document.getElementById('search-input');
    searchInput.disabled = true;
}

function openSearch() {
    document.getElementById('background-fader').style.display = 'block';
    document.getElementById('search-modal').style.display = 'block';
    let searchInput =  document.getElementById('search-input');
    searchInput.disabled = false;
    searchInput.focus();
    searchInput.select();
}

function siteSearchListener() {
    const menuResults =  document.getElementById('search-results');
    searchItems = menuResults.querySelectorAll(".search-result");

    return function (event) {
        const searchString = this.value.toLowerCase();
        searchItems.forEach(function (result) {
            const siteSearchText = result.querySelector(".name").textContent.toLowerCase();
            if (event.keyCode !== 38 && event.keyCode !== 40 && event.keyCode !== 9) {
                result.querySelector("a").classList.remove('selected');
            }

            if (siteSearchText.includes(searchString)) {
                result.style.display = 'block';
            } else {
                result.style.display = 'none';
            }
        });

        if (event.keyCode !== 38 && event.keyCode !== 40 && event.keyCode !== 9) {
            selectedIndex = -1;
            results = Array.from(document.querySelectorAll(".search-result a")).filter(function(result) {
                return result.parentElement.style.display !== 'none';
            });
        }
    };
}

window.onload = init;