//console.log("bank Grid JS loaded")
//
//window.addEventListener('load', () => {
//    const bankGrid = document.getElementById('bankGrid');
//    const totalAmountSpan = document.getElementById('totalAmountSpan');
//    console.log("here");
//
//    if (bankGrid && totalAmountSpan) {
//        console.log("here2");
//        bankGrid.addEventListener('selection-changed', (event) => {
//            const selectedItems = event.detail.value;
//            let totalAmount = 0;
//
//            selectedItems.forEach(item => {
//                const credit = parseFloat(item.credit);
//                const debit = parseFloat(item.debit);
//                totalAmount += (credit - debit);
//            });
//
//            totalAmountSpan.textContent = `Total Amount: ${totalAmount}`;
//        });
//    }
//});