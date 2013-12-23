$.getJSON( "/blended-with.json", function( data ) {
  var items = [];
  $.each( data, function( index, item ) {
    items.push( "<li id='" + item.id + "'>" + item.name + "</li>" );
  });
 
  $( "<ul/>", {
    "class": "my-blend",
    html: items.join( "" )
  }).appendTo( "#blends" );
});

