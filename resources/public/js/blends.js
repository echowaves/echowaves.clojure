
$.getJSON( "/blended-with.json", function( data ) {
  var items = [];
  $.each( data, function( index, item ) {
    items.push( "<li id='" + item.id + "'>" + item.name + "</li>" );
  });
 
  $("#blended-with").append(items);
});

$.getJSON( "/unconfirmed-blends.json", function( data ) {
  var items = [];
  $.each( data, function( index, item ) {
    items.push( "<li class='unconfirmed-blend' id='" + item.wave_id2 + "'>" + item.name + "</li>" );
  });
    
    $("#unconfirmed-blends").append(items);
});

