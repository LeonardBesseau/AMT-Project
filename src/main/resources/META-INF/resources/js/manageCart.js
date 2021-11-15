const KEY = "cart"

function storeProduct(productName, productPrice) {
    // Check if cart exists
    let cart = window.localStorage.getItem(KEY);
    let product = {name: productName, price: productPrice, quantity : 1};
    if (cart === null) {
        // Add new cart with product
        cart = [product]
    } else {
        let productFound = false;
        // Retrieve the cart
        cart = JSON.parse(cart);
        // Check for product in the cart
        for (let product of cart) {
            // If product found, increase quantity
            if (product.name === productName) {
                product.quantity++;
                productFound = true;
                break;
            }
        }
        // Add new product to the cart if not found
        if (!productFound) {
            cart.push(product)
        }
    }
    window.localStorage.setItem(KEY, JSON.stringify(cart));
}


function retrieveCart() {


    // Check if already in local storage
    let product = window.localStorage.getItem(productName);
    if (product === null) {
        // Add to cart
        product = {name: productName, price: productPrice, quantity : 1};

    } else {
        // Increase quantity
        product = JSON.parse(product);
        product.quantity++;
    }
    window.localStorage.setItem(productName, JSON.stringify(product));
}
