var mainPageApp = angular.module('mainPageApp',[]);

mainPageApp.controller('mainController', ['$scope', function($scope){
	$scope.templates = 
		[ { name: 'header', url: '/AllThatMusicGear/html-resources/header.html'},
		  { name: 'questionThread', url: '/AllThatMusicGear/html-resources/questionsthread.html'} ];
	$scope.header = $scope.templates[0];
	$scope.questionThread = $scope.templates[1];
}]);

var loggedInUser = "null";
var checkLogin = function () {
	$.get("UserServlet/GetSessionInfo", function(data, status){
		if (data.nickName == "null"){		
			window.location.href = '/AllThatMusicGear/login.html';		
		}
		else{
			loggedInUser = jQuery.parseJSON(data).nickName;
		}
	});
};

window.onhashchange = function() {
	window.location.reload();
};

mainPageApp.directive('voteButton', function ($compile) {
    return {
    	restrict: 'C',
    	link: function (scope, element, attrs) {
	    	var id = element[0].id;
	    	var buttonClass;
	    	if (id =="voteQuestionUp"){
	    		buttonClass = scope.buttonClass(scope.qstn.qst.loggedUserVote, true);
	    	}
	    	else if(id == "voteQuestionDown"){
	    		buttonClass = scope.buttonClass(scope.qstn.qst.loggedUserVote, false);
	    	}
	    	else if (id == "voteAnswerUp"){
	    		buttonClass = scope.buttonClass(scope.anwr.loggedUserVote, true);
	    	}
	    	else if (id == "voteAnswerDown"){
	    		buttonClass = scope.buttonClass(scope.anwr.loggedUserVote, false);
	    	}
	    	
	    	$(element).addClass(buttonClass);
	    	if (buttonClass.indexOf("disabled") > -1 
	    			|| buttonClass.indexOf("primary") > -1 
	    			|| buttonClass.indexOf("danger") > -1){
	    		element.attr("disabled", "disabled");
    	}
    },};
});

