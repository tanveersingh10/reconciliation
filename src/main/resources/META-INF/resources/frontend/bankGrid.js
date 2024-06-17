console.log("loaded 1");
window.updateListeners = function updateListeners() {
    console.log("loaded");
    const handleCheckboxClick = () => {
        console.log('inline');
        const totalAmountSpan = document.querySelector('#totalAmountSpan');
        console.log('totalAmountSpan:', totalAmountSpan);
        let totalAmount = 0;
        console.log('Initial totalAmount:', totalAmount);
        const checkboxes = document.querySelectorAll('#bankGridDiv vaadin-checkbox');
        console.log('checkboxes:', checkboxes);

        checkboxes.forEach((checkbox, index) => {
            console.log('Adding event listener to checkbox ' + index + ':', checkbox);
            checkbox.addEventListener('click', (event) => {
                console.log('Click event triggered for checkbox', index);
                const grid = document.querySelector('#bankGridDiv #bankGrid');
                console.log('Grid:', grid);
                if (grid) {
                    const activeRowGroup = grid._activeRowGroup;
                    console.log('activeRowGroup:', activeRowGroup);
                    if (activeRowGroup) {
                        const key = index - 1;
                        console.log('key:', key);
                        const x = 27 + ((key - 1) * 6);
                        console.log('x:', x);
                        const amountCell = activeRowGroup.childNodes[key].children['vaadin-grid-cell-' + x]._content.firstChild.firstChild.textContent;
                        console.log('amountCell:', amountCell);
                        const amount = parseFloat(amountCell) || 0;  // Default to 0 if parseFloat fails
                        console.log('Parsed amount:', amount);
                        if (checkbox.checked) {
                            totalAmount += amount;
                        } else {
                            totalAmount -= amount;
                        }
                        console.log('Updated totalAmount:', totalAmount);
                        let roundedAmount = parseFloat(totalAmount.toFixed(2));
                        if (roundedAmount === -0) roundedAmount = 0;
                        totalAmountSpan.textContent = 'Total Amount: ' + roundedAmount;
                        console.log('Updated totalAmountSpan:', totalAmountSpan.textContent);
                    } else {
                        console.log('activeRowGroup not found');
                    }
                } else {
                    console.log('Grid not found');
                }
            });
        });
    };

    // Function to observe changes in the grid
//    const observeGridChanges = () => {
//        const grid = document.querySelector('#bankGrid');
//        if (grid) {
//            const observer = new MutationObserver((mutationsList) => {
//                for (const mutation of mutationsList) {
//                    if (mutation.type === 'childList' || mutation.type === 'attributes') {
//                        console.log('Grid changed, reinitializing event listeners');
//                        handleCheckboxClick();
//                        break;
//                    }
//                }
//            });
//
//            observer.observe(grid, { attributes: true, childList: true, subtree: true });
//        } else {
//            console.log('#bankGrid not found');
//        }
//    };

    // Initialize the event listeners and observer on DOM content loaded
    handleCheckboxClick();
//    observeGridChanges();
}
