function sort(newFilter){
  if( typeof this.filters === 'undefined'){
    this.filters = []; // remember activated filters
  }
  if(this.filters.includes(newFilter)){
    // remove filter
    this.filters = this.filters.filter((item) => {return item !== newFilter});
    [...document.getElementsByClassName("category-selector-"+newFilter)].forEach((item) =>{
      item.style.background = "transparent"
    });
  }else{
    // add filter
    this.filters.push(newFilter);
    [...document.getElementsByClassName("category-selector-"+newFilter)].forEach((item) =>{
      console.log(item.style.background)
      item.style.background = "#369a24"
    });
  }

  // hide all
  [...document.getElementsByClassName("toFilter")].forEach((item) =>{
    item.style.display = "none"
  });

  // display selected
  [...document.getElementsByClassName("toFilter")].filter((item) => {
    for (let filter of this.filters){
      if (item.className.indexOf(filter) === -1){
        return false;
      }
    }
    return true;
  }).forEach((item) => {
    item.style.display = "block"
  });
}