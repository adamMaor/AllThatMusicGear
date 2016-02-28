/**
 * navBarController - Controller for the navbar elements, handles showing current logged in user information, handling question 
 * submission and marking active state for navbar elements.
 */

angular.module('mainPageApp').controller('navBarController', ['$scope', '$http', '$location', function($scope, $http, $location) {
	$scope.qTopics = [];
	
	// get logged in user information to display on navbar
	$scope.loggedInUserInfo = function(){
		$http.get("UserServlet/GetSessionInfo")
		.success(function(response) {
			if (response.nickName != "null"){
				$scope.loggedInUserNickName = response.nickName;	
				$scope.loggedInUserPhotoURL = response.photoURL;
			}
			else {
				window.location.href = '/AllThatMusicGear/login.html';
			}
		})
	}
	$scope.loggedInUserInfo();
	
	// show bootstrap popovers when document finishes loading
	$(document).ready(function(){
	    $('[data-toggle="popover"]').popover(); 
	});
	
	// set active state for the navbar items - marked effect
	var location = window.location.pathname;
	if (location == "/AllThatMusicGear/leaderboard.html"){
		$("#leaderNav").addClass( "active" );
	}
	else if (location == "/AllThatMusicGear/profile.html"){
		$("#profileNav").addClass( "active" );
	}
	else if (location == "/AllThatMusicGear/topics.html"){
		$("#topicsNav").addClass( "active" );
	}
	else{
		var hash = $location.path();
		if (hash == "/bytopic"){
			$("#topicsNav").addClass( "active" );
		}
		else if(hash =="/newquestions"){
			$("#newQNav").addClass( "active" );
		}
		else if(hash == "/topquestions"){
			$("#allQNav").addClass( "active" );
		}	
	}
	
	// question submission method upon user clicking submit
	$scope.submitQuestion = function(){
		checkLogin();
		var topicString = "";
		//prepare a ',' delimited string for the server 
		for (var i =0; i < $scope.qTopics.length; i++){
			topicString += $scope.qTopics[i] + ",";
		}
		// if user hasn't written ',' on the last topic, we grab what he's written too
		if ($scope.qTopic != "")
			topicString += $scope.qTopic;
		var parameters = {
				params: {
					qText: $scope.qText,
					topicList: topicString,						
				}
		};
		$http.get("QandAServlet/InsertQuestion", parameters);
		$scope.resetQFields();
		$('#askQuestionModal').modal('hide');
	}
	
	// on every keypress on topic field, check if the last char is ',' signaling a complete topic to register
	$scope.checkQuestionTopic = function(){
		if($scope.qTopic[$scope.qTopic.length-1] == ','){
			if ($scope.qTopic.length>1){
				// remove trailing ',' then trim whitespaces
				$scope.qTopic = $scope.qTopic.slice(0,-1).trim();
				// check if topic exists in topic list
				if ($scope.qTopics.indexOf($scope.qTopic) == -1){
					$scope.qTopics.push($scope.qTopic);				
				}
			}	
			$scope.qTopic = "";
		}
	}
	
	$scope.resetQFields = function(){
		$scope.qText = "";
		$scope.qTopics = [];
	}
	
	$scope.logOut = function(){
		$http.post("UserServlet/LogOut");
	}
 }]);