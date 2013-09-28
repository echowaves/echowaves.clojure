function colorStr(color) {
	return "rgb("+color[0]+","+color[1]+","+color[2]+")";
}

function setColor(div, colors) {	
    var bgColor = colors[0];
    var textColor = colors[1];    
	div.css("background-color", colorStr(bgColor));
	div.find('a').css("color", colorStr(textColor));
}

$(document).ready(function(){

    $(".thumbnail")
        	.each(function() {
             var div = $(this);
             var url = div.find('img').attr('src');
             var thumbColors = new AlbumColors(url);
		
            var color = ""; 		
            thumbColors.getColors(function(colors) {
            setColor(div, colors);
        });
    });	
});
