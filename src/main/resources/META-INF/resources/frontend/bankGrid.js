window.updateListeners = function updateListeners() {
    const handleCheckboxClick = () => {
        const totalAmountSpan = document.querySelector('#bankLayout #totalAmountSpan');
        let totalAmount = 0;
        const checkboxes = document.querySelectorAll('#bankLayout #bankGridDiv vaadin-checkbox');

        checkboxes.forEach((checkbox, index) => {
                checkbox.addEventListener('click', (event) => {
                const grid = document.querySelector('#bankLayout #bankGridDiv #bankGrid');
                if (grid) {
                    const activeRowGroup = grid._activeRowGroup;
                    if (activeRowGroup) {
                        const key = index - 1;
                        const x = 27 + ((key - 1) * 6);
                        const amountCell = activeRowGroup.childNodes[key].children['vaadin-grid-cell-' + x]._content.firstChild.firstChild.textContent;
                        const amount = parseFloat(amountCell) || 0;  // Default to 0 if parseFloat fails
                        if (checkbox.checked) {
                            totalAmount += amount;
                        } else {
                            totalAmount -= amount;
                        }
                        let roundedAmount = parseFloat(totalAmount.toFixed(2));
                        if (roundedAmount === -0) roundedAmount = 0;
                        if (roundedAmount >= 0) {
                            totalAmountSpan.textContent = 'Credit: ' + roundedAmount;
                        } else {
                            totalAmountSpan.textContent = 'Debit: ' + Math.abs(roundedAmount);
                        }
                    } else {
                        console.log('activeRowGroup not found');
                    }
                } else {
                    console.log('Grid not found');
                }
            });
        });
    };

    handleCheckboxClick();

}

window.updateStyles = function updateStyles() {
    const gridCells = document.querySelectorAll('#bankGridDiv #bankGrid vaadin-grid-cell-content');

    gridCells.forEach(cell => {
        const parent = cell.parentElement;
        if (parent.classList.contains('reconciled')) {
            parent.style.backgroundColor = '#d4edda'; // table-success
            parent.style.color = '#155724';
        } else if (parent.classList.contains('unreconciled')) {
            parent.style.backgroundColor = '#f8d7da'; // table-danger
            parent.style.color = '#721c24';
        }
    });
};


window.updateListenersInvoice = function updateListenersInvoice() {
    const handleCheckboxClick = () => {
        const totalAmountSpan = document.querySelector('#invoiceLayout #totalAmountSpan');
        console.log(totalAmountSpan);
        let totalAmount = 0;
        const checkboxes = document.querySelectorAll('#invoiceGridDiv vaadin-checkbox');

        checkboxes.forEach((checkbox, index) => {
                console.log(checkbox);
                checkbox.addEventListener('click', (event) => {
                const grid = document.querySelector('#invoiceGridDiv #invoiceGrid');
                console.log(grid);
                if (grid) {
                    const activeRowGroup = grid._activeRowGroup;
                    if (activeRowGroup) {
                        const key = index - 1;
                        const x = 27 + ((key - 1) * 6);
                        console.log(x);
                        const amountCell = activeRowGroup.childNodes[key].children['vaadin-grid-cell-' + x]._content.firstChild.textContent;
                        console.log(amountCell);
                        const amount = parseFloat(amountCell) || 0;  // Default to 0 if parseFloat fails
                        console.log(amount);
                        if (checkbox.checked) {
                            totalAmount += amount;
                        } else {
                            totalAmount -= amount;
                        }
                        let roundedAmount = parseFloat(totalAmount.toFixed(2));
                        if (roundedAmount === -0) roundedAmount = 0;
                        totalAmountSpan.textContent = 'Invoice Amount: ' + roundedAmount;
                    } else {
                        console.log('activeRowGroup not found');
                    }
                } else {
                    console.log('Grid not found');
                }
            });
        });
    };

    handleCheckboxClick();
}