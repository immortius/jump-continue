'use strict';

var themes = []
var themeLookup = {}
var draft = {};
var atlasScale = 0.5;

function loadData(content) {
  themes = content.themes;
  themes.forEach(theme => {
    themeLookup[theme.id] = theme;
    theme.available = true;
  });
  setupSite();
}

function setupSite() {
  setupMenuItems();
  setupConfiguration();
  $('#start-draft').click(function(event) {
    event.preventDefault();
    beginDraft();
  }.bind(this));
  $('#draft-pick-theme').click(function(event) {
    event.preventDefault();
    draftPick();
  }.bind(this));
  $('.draft-reset').click(function(event) {
    event.preventDefault();
    resetDraft();
  }.bind(this));
}

function setupMenuItems() {
	$('.nav li').click(function() {
		$('.page').toggleClass('hidden', true);
		$('.nav a').toggleClass('active', false);
		var link = $(this).find('a');
		link.toggleClass('active', true);
		$(link.attr('href')).toggleClass('hidden', false);
	});
}

/** Config Menu **/

function setupConfiguration() {
  let configPage = $('#config-themes');
  let content = "";
  themes.forEach(theme => {
    content += '<div id="config-theme-' + theme.id + '"><input id="config-theme-select-' + theme.id + '" class="config-theme-select" type="checkbox" checked> <a href="">' + theme.name;
    if (theme.variant > 0) {
      content += ' (' + theme.variant + ')';
    }
    content += '</a></div>';
  });
  configPage.append(content);
  
  themes.forEach(theme => {
    $('#config-theme-' + theme.id + ' input').change(function() {
      event.preventDefault();
      themeLookup[theme.id].available = this.checked;
      updateThemeCode();
    });
    $('#config-theme-' + theme.id + ' a').click(function() {
      event.preventDefault();
      let title = theme.name;
      if (theme.variant != 0) {
        title += ' (' + theme.variant + ')';
      }
      $('#config-variation-title').text(title);
      let variationContainer = $('#config-variation-cards');
      variationContainer.empty();
      let variationCards = "";
      theme.keycards.forEach(card => {
        variationCards += generateCardDisplay(card);
      });
      variationContainer.append(variationCards);
    });    
  });
    
  $('#config-theme-select-all').click(function(event) {
    event.preventDefault();
    $(".config-theme-select").prop("checked", true);
    themes.forEach(theme => theme.available = true);
    updateThemeCode();
  }.bind(this));
  $('#config-theme-clear-all').click(function(event) {
    event.preventDefault();
    $(".config-theme-select").prop("checked", false);
    themes.forEach(theme => theme.available = false);
    updateThemeCode();
  }.bind(this));
  $('#config-theme-code').change(function(event) {
		event.preventDefault();
    themeCodeChange();
	});
  if (storageAvailable('localStorage')) {
		if (localStorage.getItem('code')) {
			let code = localStorage.getItem('code');
      if (code) {
        $('#config-theme-code').val(code); 
        themeCodeChange();
      }
		} 
	} else {
    updateThemeCode();
  }
}

function updateThemeCode() {
  let rawCode = 0n;

  themes.forEach(theme => {
    if (theme.available) {
      rawCode = rawCode | (1n << BigInt(theme.id));
    }
  });
  let code = rawCode.toString(36);
  $('#config-theme-code').val(code);
  
	if (storageAvailable('localStorage')) {
		localStorage.setItem('code', code); 
	}
}

function themeCodeChange() {
  $(".config-theme-select").prop("checked", false);
  themes.forEach(theme => theme.available = false);  
  
  let code = $('#config-theme-code').val().trim();
  let rawCode = parseBigInt(code, 36);
  let id = 0;
  while (rawCode > 0) {
    let theme = themeLookup[id++];
    if (theme != null && (rawCode & 1n) == 1n) {
      $('#config-theme-select-' + theme.id).prop('checked', true);
      theme.available = true;
    }
    rawCode = rawCode >> 1n;
  } 
  
  if (storageAvailable('localStorage')) {
		localStorage.setItem('code', code); 
	} 
}

/** Setup Draft **/

function beginDraft() { 
  $('#error-message').toggleClass('hidden', true);
  $('#draft-options').toggleClass('hidden', true);
  $('#draft-area').toggleClass('hidden', false);
  
  let poolSize = $('#select-pool-size').val();
  let numPlayers = $('#num-players').val();
  let freshSecondPool = $('#fresh-pool-second-pick').is(':checked');
    
  if (numPlayers < 1) {
    $('#error-message').text("At least one player required.");
    $('#error-message').toggleClass('hidden', false);
    $('#draft-options').toggleClass('hidden', false);
    $('#draft-area').toggleClass('hidden', true);
    return;
  }
  
  if (poolSize < 2) {
    $('#error-message').text("Pool size must be at least 2.");
    $('#error-message').toggleClass('hidden', false);
    $('#draft-options').toggleClass('hidden', false);
    $('#draft-area').toggleClass('hidden', true);
    return;
  }
    
  buildDraftState(numPlayers);
  
  let themesRequired = calcThemesRequired(poolSize, numPlayers, freshSecondPool);
  if (draft.unpickedThemes.length < themesRequired) {
    $('#error-message').text("Not enough themes available - " + themesRequired + " themes are required, but only " + draft.unpickedThemes.length + " available.");
    $('#error-message').toggleClass('hidden', false);
    $('#draft-options').toggleClass('hidden', false);
    $('#draft-area').toggleClass('hidden', true);
    return;
  }
    
  if (poolSize == 2 && !freshSecondPool) {
    generateRandomSelection();
    displayDraftResults();
  } else {
    assignPools(poolSize, freshSecondPool);
    displayNextDraftPick();
  }
}

function calcThemesRequired(poolSize, numPlayers, freshSecondPool) {
  let themesRequired = numPlayers * poolSize;
  if (freshSecondPool) {
    themesRequired += numPlayers * poolSize;
  }
  return themesRequired;
}


function buildDraftState(numPlayers) {
   draft = {
     currentPlayer : 0,
     currentPick : -1,
     players : [],
     unpickedThemes : []
   };
   for (let i = 0; i < numPlayers; i++) {
     draft.players.push(
     {
       id : i + 1, 
       selection : [],
       poolOne : [],
       poolTwo : []
     })
   }
   draft.unpickedThemes = createThemePool();
}

function generateRandomSelection() {
  draft.players.forEach(player => player.selection.push(dealTheme()));
  draft.players.forEach(player => player.selection.push(dealTheme()));
}

function dealThemes(number) {
  let result = [];
  for (let i = 0; i < number; i++) {
    result.push(dealTheme());
  }
  return result;  
}  

function dealTheme() {
  let pickIndex = Math.floor(Math.random() * draft.unpickedThemes.length);
  let pick = draft.unpickedThemes.splice(pickIndex, 1)[0];
  return pick;
}

function createThemePool() {
  let result = [];
  themes.forEach(theme => {
    if (theme.available) { 
      result.push(theme);
    }
  });
  return result;
}

function assignPools(poolSize, freshSecondPool) {
  draft.players.forEach(
    player => {
      player.poolOne = dealThemes(poolSize);
    }
  );
  if (freshSecondPool) {
    draft.players.forEach(
      player => {
        player.poolTwo = dealThemes(poolSize);
      }
    );
  } else {
    draft.players.forEach(
      player => {
        player.poolTwo = player.poolOne;
      }
    );
  }
}

/** Progress Draft **/

function resetDraft() {
  $('#draft-options').toggleClass('hidden', false);
  $('#draft-area').toggleClass('hidden', true);
  $('#draft-results-pane').toggleClass('hidden', true);
}

function draftPick() {
  if (draft.currentPick == -1) {
    return;
  }
  let player = draft.players[draft.currentPlayer];
  let firstPick = (player.selection.length == 0);
  let pool = []; 
  
  if (firstPick) {
    pool = player.poolOne;
  } else {
    pool = player.poolTwo;
  }
  let pick = pool.splice(draft.currentPick, 1)[0];
  player.selection.push(pick);
  draft.currentSelection = -1;
  
  if (!firstPick) {
    draft.currentPlayer++;
  }
  if (draft.currentPlayer >= draft.players.length) {
    displayDraftResults();
  } else {
    displayNextDraftPick();
  }  
}

/** Draft Display **/

function displayNextDraftPick() { 
  let player = draft.players[draft.currentPlayer]
  let firstPick = (player.selection.length == 0);
  $('#draft-player-number').text(player.id);
  $('#draft-pick-number').text(player.selection.length + 1);
  
  if (firstPick) {
    $('#draft-pick-1').empty();
    $('#draft-picked').addClass("hidden");
    displayDraftPool(player.poolOne);
  } else {
    $('#draft-picked').removeClass("hidden");
    $('#draft-pick-1').append(generateThemeDisplay(player.selection[0], false));
    $('#draft-pick-1 .card-icon').addClass('picked');
    displayDraftPool(player.poolTwo);
  }
}

function displayDraftPool(pool) {
  let draftSelectionDisplay = $("#draft-selection");
  draftSelectionDisplay.empty();
  let selectionHtml = '<div class="theme-list">'
  for (let i = 0; i < pool.length; i++) {
    selectionHtml += '<div id="draft-choice-' + i + '" class="draft-choice">';
    selectionHtml += generateThemeDisplay(pool[i], false);
    selectionHtml += '</div>'
  }
  selectionHtml += '</div>'
  draftSelectionDisplay.append(selectionHtml);
  for (let i = 0; i < pool.length; i++) {
    $('#draft-choice-' + i + ' .card-icon').click(function(event) {
      draft.currentPick = i;
      $('.draft-choice .card-icon').removeClass('selected');
      $('#draft-choice-' + i + ' .card-icon').addClass('selected');
    });    
  }
}

function displayDraftResults() {
  $('#draft-area').toggleClass('hidden', true);
  $('#draft-results-pane').toggleClass('hidden', false);
  $("#draft-results").empty();
  $("#draft-variation-title").text("");
  $("#draft-variation-cards").empty();
  
  draft.players.forEach(player => {
    let content = "<h2 class='player-name'>Player " + player.id + "</h2><div class='theme-list'>"
    content += generateThemeDisplay(player.selection[0], true, player.id + '-0');
    content += generateThemeDisplay(player.selection[1], true, player.id + '-1');
    content += '</div>'
    $("#draft-results").append(content);
  });
  
  draft.players.forEach(player => {
    for (let i = 0; i < 2; i++) {
      let theme = player.selection[i];
      $('#' + player.id + '-' + i).click(function() {
        event.preventDefault();
        let title = theme.name;
        if (theme.variant != 0) {
          title += ' (' + theme.variant + ')';
        }
        $('#draft-variation-title').text(title);
        let variationContainer = $('#draft-variation-cards');
        variationContainer.empty();
        let variationCards = "";
        theme.keycards.forEach(card => {
          variationCards += generateCardDisplay(card);
        });
        variationContainer.append(variationCards);
      }); 
    }
  });    
}

/** Card and Theme Display **/

function generateThemeDisplay(theme, includeVariant, id) {
  console.log(theme);
  let label = theme.name;
  if (includeVariant && theme.variant > 0) {
    label += ' (' + theme.variant + ')';
  }
    
  let result = generateImageDisplay(theme.displayImage, label, id);
  result += '<div class="theme-label"><div class="theme-label-text">' + label + '</div></div>';  
  result += '</div>';
  return result;
}

function generateCardDisplay(card) {
  return generateImageDisplay(card.image, card.name) + '</div>'; 
}

function generateImageDisplay(image, title, id) {
  let result = '<div class="card-icon" style="';
	result += 'background-position: -' + atlasScale * (image.xOffset) + 'px -' + atlasScale *(image.yOffset) + 'px;';
  result += 'width: ' + atlasScale *(image.width) + 'px; height: ' + atlasScale *(image.height) + 'px;"';
  if (id) {
    result += 'id="' + id + '"';
  }
	result += ' title="' + title + '">';
  return result;
}

/** Utility **/

function parseBigInt(value, radix) {
    return [...value.toString()]
        .reduce((r, v) => r * BigInt(radix) + BigInt(parseInt(v, radix)), 0n);
}

function storageAvailable(type) {
	try {
		var storage = window[type],
			x = '__storage_test__';
		storage.setItem(x, x);
		storage.removeItem(x);
		return true;
	}
	catch(e) {
		return false;
	}
}

/** Begin **/

$(function() {
  $.get('./data/content.json', function(responseText) {
    loadData(responseText);
  });
});


