/**
 * 
 */

angular.module('mainPageApp').controller('navBarController', ['$scope', '$http', function($scope, $http) {
	$scope.qTopics = [];
	
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
	
	$(document).ready(function(){
	    $('[data-toggle="popover"]').popover(); 
	});
	
	var location = window.location.pathname;
	if (location.toLocaleString().indexOf("leaderboard", 16) != -1){
		$("#leaderNav").addClass( "active" );
	}
	else if (location.toLocaleString().indexOf("profile", 16) != -1){
		$("#profileNav").addClass( "active" );
	}
	else{
		var hash = window.location.hash;
		if (location.toLocaleString().indexOf("topic", 16) != -1 || hash.toLocaleString().indexOf("topic", 0) != -1){
			$("#topicsNav").addClass( "active" );
		}
		else if(hash.toLocaleString().indexOf("newquestions", 0) != -1){
			$("#newQNav").addClass( "active" );
		}
		else if(hash.toLocaleString().indexOf("topquestions", 0) != -1){
			$("#allQNav").addClass( "active" );
		}
		
	}
	
	$scope.submitQuestion = function(){
		checkLogin();
		var topicString = "";
		//prepare a ',' delimited string for the server 
		for (var i =0; i < $scope.qTopics.length; i++){
			topicString += $scope.qTopics[i] + ",";
		}
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