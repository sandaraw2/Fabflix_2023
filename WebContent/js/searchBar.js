const searchInput = document.getElementById("search-bar");
const autocompleteResults = document.getElementById("autocomplete-results");
const searchButton = document.getElementById('search-button');
let delayTimer;
let selectedIndex = -1;
// Event listener for input changes


 searchInput.addEventListener("input", function() {
   const inputValue = searchInput.value.toLowerCase();
   // Clear the previous timeout
   clearTimeout(delayTimer);
   // Only perform Autosearch if input length has greater than 2 characters
   if (inputValue.length >= 3) {
     // after 300ms check for results in cache or send Autocomplete search request to servlet
     // * setTimeout returns a timeoutId that can be used to clearTimeout
     delayTimer = setTimeout(() => {
       //print to console
       console.log("The Autocomplete search is initiated");
       //check cache results
       let results = getFromCache(inputValue)
       if(results != null){
         //print to console
         console.log("AutoSearchResults from cache results")
         //if results exist on cache display those
         displayAutoSearchResults(results)
       }else{
         //print to console
         console.log("AutoSearchResults from ajax request to server")
         // send request to AutoSearch servlet
         jQuery.ajax({
           dataType: "json",
           method: "GET",
           url: "api/search?title=" + inputValue,
           success: (resultData) => {
             // Cache the Autocomplete results
             cacheResults(inputValue, resultData);
             displayAutoSearchResults(resultData);
           }
         });
       }
     }, 300); // 300ms delay
   } else {
     // If the input is less than 3 characters, clear the autocomplete results
     autocompleteResults.innerHTML = "";
   }
});

 searchInput.addEventListener('keydown', function(event){
   const autoSuggestions = document.querySelectorAll(".autocomplete-suggestion");
   if (event.key === "ArrowDown") {
     // update selected index down
     selectedIndex ++;
     //ensure the selected index does not go over max suggestion list index
     if(selectedIndex > autoSuggestions.length -1){
       selectedIndex = autoSuggestions.length -1;
     }
     //update selected suggestion visually according to updated index
     updateSelected()
   }else if(event.key === "ArrowUp"){
     // update selected index up
     selectedIndex --;
     //ensure the selected index does not go past smallest suggestion list index
     if(selectedIndex < 0){
       selectedIndex = 0;
     }
     //update selected suggestion visually according to updated index
     updateSelected()
   }else if(event.key === "Enter"){

     if (selectedIndex !== -1) {
       //if user selects any autocomplete suggestion and presses enter key
       //get current selected autoComplete item
       const selectedSuggestion = autoSuggestions[selectedIndex];
       const movieId = selectedSuggestion.dataset.movieId;
            window.location.href = 'single-movie.html?id=' + movieId;
     }else{
       //if user does not select any autocomplete suggestions and presses enter key
       //display full text search results
       const inputValue = searchInput.value.toLowerCase();
       // send request to AutoSearch servlet
       jQuery.ajax({
         dataType: "json",
         method: "GET",
         url: "api/search?title=" + inputValue,
         success: (resultData) => {
           displaySearchResults();
         }
       });
     }
   }
 });

 searchButton.addEventListener('click', function() {
   const title = searchInput.value.toLowerCase();
   // Makes the HTTP GET request and registers on success callback function handleStarResult
   jQuery.ajax({
     dataType: "json", // Setting return data type
     method: "GET", // Setting request method
     url: "api/search?title=" + title,// Setting request url
     success: () => displaySearchResults() // Setting callback function to handle data returned successfully by the StarsServlet
   });
 });


 function displaySearchResults() {
   // Redirect to the movie link
   window.location.href = "movielist.html";
 }

 function displayAutoSearchResults(results) {
   autocompleteResults.innerHTML = "";
   if (results.length === 0) {
     autocompleteResults.style.display = "none";
     return;
   }

   results.forEach(result => {
     const autoCompleteItem = document.createElement("div");
     autoCompleteItem.textContent = result["movie_title"];
     autoCompleteItem.classList.add("autocomplete-suggestion");
     autoCompleteItem.dataset.movieId = result["movie_id"];

     autoCompleteItem.addEventListener("click", function() {
       // Redirect to the movie link
       window.location.href = 'single-movie.html?id=' + result["movie_id"];
     });

     //add mouse event listener to each autoComplete item for hovering action
     autoCompleteItem.addEventListener("mouseover", () => {
       // gets a live list of autoComplete items of autoComplete results
       const autoCompleteList = Array.from(autocompleteResults.children);
       selectedIndex = autoCompleteList.indexOf(autoCompleteItem);
       //update selected autoComplete item visually
       updateSelected();
     });

     //append resultItem to autocompleteResults which refreshes it
     autocompleteResults.appendChild(autoCompleteItem);
   });

   autocompleteResults.style.display = "block";
 }

function updateSelected() {
  const autoSuggestions = document.querySelectorAll(".autocomplete-suggestion");

  autoSuggestions.forEach((suggestion, index) => {
    //go through each autoComplete item and update visually
    if (index === selectedIndex) {
      //highlight  autocomplete item if it is the selected one
      // adds ".selected" behind autocomplete-suggestion to follow selected css defined in html
      suggestion.classList.add("selected");
      searchInput.value = suggestion.textContent.trim();
    } else {
      //unhighlight the not selected autocomplete item
      suggestion.classList.remove("selected");
    }
  });
}

function cacheResults(query, results) {
   // create cacheKey
  const autoCacheKey = "autocomplete_" + query;
  // store results as cacheValue
  const autoCacheValue = JSON.stringify(results);
  // set the key:value pair in session storage
  sessionStorage.setItem(autoCacheKey, autoCacheValue);
}

function getFromCache(query) {
   // get autoCacheKey
  const autoCacheKey = "autocomplete_" + query;
  // search for value
  const autoCacheValue = sessionStorage.getItem(autoCacheKey);
  // return cacheValue if found else null
  return autoCacheValue ? JSON.parse(autoCacheValue) : null;
}

