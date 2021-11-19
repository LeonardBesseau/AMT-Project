function sort(newFilter){
  if( typeof this.filters === 'undefined'){
    this.filters = [];
  }
  if(this.filters.includes(newFilter)){
    this.filters = this.filters.filter((item) => {return item !== newFilter});
    [...document.getElementsByClassName("category-selector-"+newFilter)].forEach((item) =>{
      item.style.background = "transparent"
    });
  }else{
    this.filters.push(newFilter);
    [...document.getElementsByClassName("category-selector-"+newFilter)].forEach((item) =>{
      console.log(item.style.background)
      item.style.background = "#369a24"
    });
  }

  [...document.getElementsByClassName("toFilter")].forEach((item) =>{
    item.style.display = "none"
  });

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