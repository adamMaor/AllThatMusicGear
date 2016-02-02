 
 var logInApp = angular.module('logInApp',[]);
 logInApp.controller('logInAppController', ['$scope', '$http', function($scope, $http, $window) {
		$scope.logedInUserNickName = "";
		$scope.http = $http;
		$scope.gotomainpagevis = {'visibility' : 'hidden'};
		
		$scope.logIn = function() {
			/* first check userName */
			for (x in $scope.allUsers) {
				if ($scope.allUsers[x].userName == $scope.logUserName){
					if ($scope.allUsers[x].password == $scope.logPassword){
						/* good LogIn */
						$scope.logedInUserNickName = "Current Logged In User NickName: " + $scope.allUsers[x].nickName;
						$scope.logedInUser = $scope.allUsers[x].nickName;
						$scope.logInError = "Logged in - Take care to move to main question view";
						$scope.gotomainpagevis = {'visibility' : 'visible'};
						$scope.resetLogFields();
						$window.location.href = '/AllThatMusicGear/Main.html';
						return;
					}
					else{
						$scope.logInError = "Password Incorrect";
						return;
					}	
				}
			}
			$scope.logInError = "User Name Doesn't Exist";
		}
		
		$scope.register = function(){ 
			
			if ($scope.regUserName == "" || $scope.regPassword =="" || $scope.regNickName== ""){
				$scope.registerError = "Must Fill UserName + Password + NickName";
				return;
			}

			$http({
				method : "POST",
				url : "http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Register",
				params: { 	userName: $scope.regUserName,
							password: $scope.regPassword,
							nickName: $scope.regNickName,
							description: $scope.regDesc,
							phtoUrl: $scope.regPhoto },
				headers: {'Content-Type': 'application/x-www-form-urlencoded'}
			}).success(function(response){
				$scope.logedInUserNickName = "Current Logged In User NickName: " + response[0].nickName;
				$scope.logedInUser = response[0].nickName;
				$scope.resetRegFields();
				$scope.updateAllUserTable();
				$scope.gotomainpagevis = {'visibility' : 'visible'};	
			})
		}
		
		$scope.updateAllUserTable = function()
		{
			$scope.http.get("http://localhost:8080/AllThatMusicGear/LogAndRegServlet/Users")
			.success(function(response) {			
				$scope.allUsers = response;
				$scope.counter = 0;
				for (x in $scope.allUsers) {
					$scope.counter = $scope.counter + 1;
				}
			});
		}
		
		$scope.updateAllQuestionsTable = function()
		{
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AllQuestions")
			.success(function(response) {			
				$scope.allQuestions = response;
				$scope.qCounter = 0;
				$scope.allAnswers = [];
				for (x in $scope.allQuestions) {
					$scope.addAnswersToMainAnsTable($scope.allQuestions[x].qID);
					$scope.qCounter = $scope.qCounter + 1;
				}
			});
		}
		
		$scope.updateNewQuestionsTable = function()
		{
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/NewQuestions")
			.success(function(response) {			
				$scope.newQuestions = response;
				$scope.qNewCounter = 0;
				for (x in $scope.newQuestions) {
					$scope.qNewCounter = $scope.qNewCounter + 1;
				}
			});
		}
		
		$scope.submitQuestion = function()
		{
			if ($scope.logedInUser ==""){
				$scope.questionError - "Must be Loged in!!!";
				return;
			}
			if ($scope.qText == ""){
				$scope.questionError - "question Must have a text!!!";
				return;
			}
			var parameters = {
					params: {
						userNickName: $scope.logedInUser,
						qText: $scope.qText,
						topicList: $scope.qTopics,						
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertQuestion", parameters)
			.success(function(response) {
				$scope.updateAllQuestionsTable();
				$scope.updateNewQuestionsTable();
				$scope.resetQFields();
			});
		}
		
		$scope.updateUserRating = function(userNickNameToUpdate)
		{
			var parameters = {
					params: {
						userNickName: userNickNameToUpdate,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/UserServlet/UpdateUserRating", parameters)
			.success(function(response) {
				$scope.updateAllUserTable();
			});
			
		}
		
		$scope.voteQuestion = function(qID, userNickName)
		{
			var parameters = {
					params: {
						qId: qID,
						changeVS: 1,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateQuestion", parameters)
			.success(function(response) {
				$scope.updateAllQuestionsTable();
				$scope.updateNewQuestionsTable();
				$scope.updateUserRating(userNickName);
			});
			

		}
		
		$scope.deVoteQuestion = function(qID, userNickName)
		{
			var parameters = {
					params: {
						qId: qID,
						changeVS: -1,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateQuestion", parameters)
			.success(function(response) {
				$scope.updateAllQuestionsTable();
				$scope.updateNewQuestionsTable();
				$scope.updateUserRating(userNickName);
			});
		}
		
		$scope.updateQuestionDueToAnswerChange = function(qID)
		{
			var parameters = {
					params: {
						qId: qID,
						changeVS: 0,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateQuestion", parameters)
			.success(function(response) {
				$scope.updateAllQuestionsTable();
				$scope.updateNewQuestionsTable();
			});
		}
			
		$scope.addAnswersToMainAnsTable = function(qId)
		{
			var parameters = {
					params: {
						qID: qId,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/AnswersOfQ", parameters)
			.success(function(response) {
				$scope.remark = "In here";
				var tempArray = response;
				for (x in tempArray){
					// TODO: This result needs to be attached to a specific question and not all answers should be united
					$scope.allAnswers.push(tempArray[x]);					
				}
			});
		}
		
		$scope.answerQuestion = function(qID, qUserNickName)
		{
			if ($scope.logedInUser == qUserNickName){
				$scope.answerError ="you cant answer your own questions!";
				return;
			}
			$scope.qToAnser = qID;
			$scope.qUserToUpdate = qUserNickName;
		}
		
		$scope.submitAnswer = function()
		{
			if ($scope.logedInUser == "") {
				$scope.answerError ="a User Must be Logged In!";
				return;
			}
			if ($scope.qToAnser == "") {
				$scope.answerError ="You Must Select a question!";
				return;
			}
			if ($scope.aText == "") {
				$scope.answerError ="Answer must have text!";
				return;
			}
			var parameters = {
					params: {
						qID: $scope.qToAnser,
						userNickName: $scope.logedInUser,
						aText: $scope.aText,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/InsertAnswer", parameters)
			.success(function(response) {
//				First of all updating the questing Rating
				$scope.updateQuestionDueToAnswerChange($scope.qToAnser);
//				now updated the answer submiter Rating 
				$scope.updateUserRating($scope.logedInUser);
				$scope.updateAllQuestionsTable();
				$scope.updateNewQuestionsTable();
				$scope.resetAFields();
			});
		}
		
		$scope.voteAnswer = function(aId, qId, aUserNickName)
		{
			var parameters = {
					params: {
						aID: aId,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateAnswerPos", parameters)
			.success(function(response) {
//				First of all updating the questing Rating
				$scope.updateQuestionDueToAnswerChange(qId);
//				 - and question submiter Rating
				for (x in $scope.allQuestions){
					if ($scope.allQuestions[x].qID == qId){
						$scope.updateUserRating($scope.allQuestions[x].qUserNickName);
						break;
					}
				}
//				now updated the answer submiter Rating 
				$scope.updateUserRating(aUserNickName);
			});
		}
		
		
		
		$scope.deVoteAnswer = function(aId, qId, aUserNickName)
		{
			var parameters = {
					params: {
						aID: aId,
					}
			};
			$scope.http.get("http://localhost:8080/AllThatMusicGear/QandAServlet/UpdateAnswerNeg", parameters)
			.success(function(response) {
//				First of all updating the questing Rating
				$scope.updateQuestionDueToAnswerChange(qId);
//				 - and question submiter Rating
				for (x in $scope.allQuestions){
					if ($scope.allQuestions[x].qID == qId){
						$scope.updateUserRating($scope.allQuestions[x].qUserNickName);
						break;
					}
				}
//				now updated the answer submiter Rating 
				$scope.updateUserRating(aUserNickName);
			});
		}
		
		$scope.resetAFields = function()
		{
			$scope.aText = "";
			$scope.qToAnser = "";
			$scope.qUserToUpdate = "";
			$scope.clearAError();
		}
		$scope.clearAError = function()
		{
			$scope.answerError ="";
		}
		
		
		$scope.resetQFields = function()
		{
			$scope.qText = "";
			$scope.qTopics = "";
			$scope.clearQError();
		}
		
		$scope.clearQError = function()
		{
			$scope.questionError ="";
		}
		
		$scope.clearLogInError = function(){
			$scope.logInError = "";
		}
		
		$scope.clearRegisterError = function(){
			$scope.registerError = "";
		}
		
		$scope.resetLogFields = function() {
			$scope.logUserName = "";
			$scope.logPassword = "";
			$scope.clearLogInError();
		}

		$scope.resetRegFields = function(){
			
			$scope.regUserName = "";
			$scope.regPassword = "";
			$scope.regNickName = "";
			$scope.regDesc = "";
			$scope.regPhoto = "";
			$scope.clearRegisterError();
		}
		
	     
	    $scope.validateUserName = function() {
	    	$scope.clearRegisterError();
	    	for (x in $scope.allUsers) {
				if ($scope.allUsers[x].userName == $scope.regUserName){
					$scope.registerError = "User Name Already Exists";
				}
	    	}
	    }
	    
	    $scope.validateNickName = function() {
	    	$scope.clearRegisterError();
	    	for (x in $scope.allUsers) {
				if ($scope.allUsers[x].nickName == $scope.regNickName){
					$scope.registerError = "NickName Already Exists";
				}
	    	}
	    }
	    $scope.logedInUser = "";
		$scope.resetLogFields();
		$scope.resetRegFields();
		$scope.resetQFields();
		$scope.resetAFields();
		$scope.updateAllUserTable();
		$scope.updateAllQuestionsTable();
		$scope.updateNewQuestionsTable();
		$scope.remark = "";
		
		
	}]);