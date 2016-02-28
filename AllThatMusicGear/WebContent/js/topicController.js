/**
 * topicsCtrl - Topic cloud controller, handles fetching topic and Tpop information and setting style rules for
 * our topic cloud according to relative Tpop compared to average for topics brought, displays 50 topics at a time.
 */

angular.module('mainPageApp').controller('topicsCtrl', ['$scope', '$http', function($scope, $http) {
	$scope.pageNum = 1;
	$scope.maxPageNum = 1;
	$scope.tagsOnPage = 50;
	var parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
	
	$scope.updateTopics = function(){
		$http.get("QandAServlet/QuestionTopicsByTpop", parameter)
		.success(function(response){
			$scope.topics = response.topics;
			$scope.topicCount = response.numTopics;
			$scope.maxPageNum = parseInt((response.numTopics-1)/$scope.tagsOnPage) + 1;
			$scope.shuffleArray($scope.topics);
			// calculate average popularity score for the tag cloud
			$scope.tPopAvg = 0;
			for (var i = 0; i<$scope.topics.length; i++){
				$scope.tPopAvg += $scope.topics[i].tPop;
			}
			$scope.tPopAvg /= $scope.topics.length;
			// if the average is negative, normalize it to positive
			// since we care about the value and not the sign
			if ($scope.tPopAvg < 0){
				$scope.tPopAvg = -$scope.tPopAvg;
			}
			// if the average is 0, make it close to 0 to avoid dividing by 0
			if($scope.tPopAvg == 0){
				$scope.tPopAvg = 0.001;
			}
		});
	};
	
	$scope.updateTopics();
	
	$scope.nextPage = function(){
		if ($scope.pageNum < $scope.maxPageNum){
			$scope.pageNum += 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}
	}
	
	$scope.prevPage = function(){
		if ($scope.pageNum > 1){
			$scope.pageNum -= 1;
			parameter = { params: { offset: ($scope.pageNum-1)*$scope.tagsOnPage, listSize: $scope.tagsOnPage} };
			$scope.updateTopics();
		}	
	}
	
	//Randomize array element order in-place.
	//In order to make the tag-cloud topics more dynamic
	//We had to choose whether they will be arranged alphabetically or randomly,
	//We thought it would be nicer to randomize and present a different cloud on each refresh
	$scope.shuffleArray = function (array) {
	    for (var i = array.length - 1; i > 0; i--) {
	        var j = Math.floor(Math.random() * (i + 1));
	        var temp = array[i];
	        array[i] = array[j];
	        array[j] = temp;
	    }
	    return array;
	}
	
	// give each topic a size between min and max depending on relative size compared to average tPop
	$scope.topicCloudStyle = function(topicPop){
		var minFontSize = 100;
		var maxFontSize = 400;
		var topicPopAboveAvgPer = (topicPop / $scope.tPopAvg) * 100;
		if (topicPopAboveAvgPer > maxFontSize){
			topicPopAboveAvgPer = maxFontSize;
		}
		if (topicPopAboveAvgPer < minFontSize){
			topicPopAboveAvgPer = minFontSize;
		}
		
		return {"font-size" : topicPopAboveAvgPer+"%",
		};
	};
}]);
