/**
 * Functions to manage a cart in the localstorage and update the corresponding view
 *
 * @file manageCart.js
 */

const KEY = "cart";
const CART_LIST_ID = "cart_list";
const PRODUCT_CLASSNAME = "cart_article";
const PRODUCT_QUANTITY_CLASSNAME = "cart_quantity_input";
const PRODUCT_PRICE_CLASSNAME = "cart_price_input";
const PRODUCT_TOTAL_CLASSNAME = "cart_total_price";

/**
 * Store a product
 * @param {String}  productName     name of the product
 * @param {String}  productPrice    price of the product
 * @param {String}  imageId         image id of the product
 * @param           quantity        quantity to store
 */
function storeProduct(productName, productPrice, imageId, quantity = 1) {
  // Check quantity
  quantity = parseInt(quantity);
  if (quantity < 1) {
    return;
  }
  // Check if cart exists
  let cart = window.localStorage.getItem(KEY);
  let product = {
    name: productName,
    price: productPrice,
    imageId: imageId,
    quantity: 1
  };
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
        product.quantity += quantity;
        productFound = true;
        break;
      }
    }
    // Add new product to the cart if not found
    if (!productFound) {
      cart.push(product)
    }
  }
  // Store cart in localstorage
  window.localStorage.setItem(KEY, JSON.stringify(cart));
}

/**
 * Update the quantity of a product in the cart
 * @param {String}  productName       name of the product
 * @param {Number}  productQuantity   quantity to to update
 */
function changeProductQuantity(productName, productQuantity) {
  let cart = JSON.parse(window.localStorage.getItem(KEY));
  if (cart !== null) {
    for (let product of cart) {
      if (product.name === productName) {
        // Update the quantity
        product.quantity += productQuantity;
        // Update the view
        const articleLayout = document.getElementById(productName);
        if (product.quantity > 0) {
          // Update view to the new quantity
          const quantityLayout = articleLayout.getElementsByClassName(
              PRODUCT_QUANTITY_CLASSNAME)[0];
          quantityLayout.value = Number(quantityLayout.value) + productQuantity;

          // Update view to the new total price
          const price = parseInt(articleLayout.getElementsByClassName(
              PRODUCT_PRICE_CLASSNAME)[0].innerHTML);
          const totalLayout = articleLayout.getElementsByClassName(
              PRODUCT_TOTAL_CLASSNAME)[0];
          totalLayout.innerHTML = (Number(quantityLayout.value)
              * price).toString()
        } else {
          // Filter the cart with product.quantity <= 0
          cart = cart.filter((product) => {
            return product.quantity > 0;
          });
          // Remove the article from the view
          articleLayout.remove();
        }
        break;
      }
    }
    // Update cart in localstorage
    window.localStorage.setItem(KEY, JSON.stringify(cart));
  }
}

/**
 * Retrieve the cart from the localstorage and update the view
 */
function retrieveCart() {
  const cart = JSON.parse(window.localStorage.getItem("cart"));
  if (cart !== null) {
    for (let product of cart) {
      let name = product.name
      let quantity = product.quantity
      let total = quantity * product.price
      document.getElementById("cart_list").innerHTML +=
          "<tr class=\"cart_article\" id=\"" + name + "\">\n" +
          " <td class=\"cart_product\">\n" +
          "  <img src=\"/image/" + product.imageId
          + "\" alt=\"Oups missing image\">\n" +
          " </td>\n" +
          " <td class=\"cart_description\">\n" +
          "  <h4><a href=\"\">" + name + "</a></h4>\n" +
          " </td>" +
          " <td class=\"cart_price\">\n" +
          "  <p class=\"cart_price_input\">" + product.price + "</p>\n" +
          " </td>\n" +
          " <td class=\"cart_quantity\">\n" +
          "  <div class=\"cart_quantity_button\">\n" +
          "   <a class=\"cart_quantity_up\" onclick=\"changeProductQuantity(\'"
          + name + "\', 1)\"> + </a>\n" +
          "   <input class=\"cart_quantity_input\" type=\"text\" name=\"quantity\" value=\""
          + quantity + "\" autocomplete=\"off\" size=\"2\">\n" +
          "   <a class=\"cart_quantity_down\" onclick=\"changeProductQuantity(\'"
          + name + "\', -1)\"> - </a>\n" +
          "  </div>\n" +
          " </td>\n" +
          " <td class=\"cart_total\">\n" +
          "  <p class=\"cart_total_price\">" + total + "</p>\n" +
          " </td>\n" +
          " <td class=\"cart_delete\">\n" +
          "  <a class=\"cart_quantity_delete\" onclick=\"removeProduct(\'"
          + name + "\')\"><i class=\"fa fa-times\"></i></a>\n" +
          " </td>\n" +
          "</tr>";
    }
  }
}

/**
 * Remove a product from the cart and update the view
 * @param {String}  productName name of the product
 */
function removeProduct(productName) {
  let cart = JSON.parse(window.localStorage.getItem(KEY));
  if (cart !== null) {
    // Filter the cart
    cart = cart.filter((product) => {
      return product.name !== productName;
    });

    // Update local storage
    window.localStorage.setItem(KEY, JSON.stringify(cart));

    // Update view
    document.getElementById(productName).remove();
  }
}

/**
 * Clear the cart and update the view
 */
function clearCart() {
  // Clear local storage
  let cart = JSON.parse(window.localStorage.getItem(KEY));
  if (cart !== null) {
    cart = [];
    window.localStorage.setItem(KEY, JSON.stringify(cart));

    // Refresh view
    location.reload();
  }
}
