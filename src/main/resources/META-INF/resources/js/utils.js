function sort(filterId, tableId, elementsTag) {
  // Declare variables
  let input, filter, ul, li, a, i, txtValue;
  input = document.getElementById(filterId);
  filter = input.value.toUpperCase();
  ul = document.getElementById(tableId);
  li = ul.getElementsByTagName(elementsTag);

  // Loop through all list items, and hide those who don't match the search query
  for (i = 0; i < li.length; i++) {
    a = li[i].getElementsByTagName("a")[0];
    txtValue = a.textContent || a.innerText;
    if (txtValue.toUpperCase().indexOf(filter) > -1) {
      li[i].style.display = "";
    } else {
      li[i].style.display = "none";
    }
  }
}