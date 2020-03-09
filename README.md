# 最新のMVVM練習用TODOアプリ

本アプリは標準的なMVVMパターンを適用して作成されてます。
JetPackからのコンポーネンツを使用してます:
- androidX
- ビューモデル
- ライブデータ
- Room (Sqlite)
- 基本的なマテリアルデザイン

本アプリでは基本的なTODOデータ
作成、編集
、削除
、タスクのステータス(未完了・完了)を行うことできます。一応、SearchViewを使ってTODO項目をライブ検索をすることも可能です。

このプロジェクトはXperia XZ1:OS9のみでしか動作確認をしていません。

確認用のAPK(Debug)+Screenshotは下記のフォルダーに添付致しました。
https://github.com/anteprocess/Todo-test-app/tree/master/app/sample



# 改善点:未対応
------

* 複数名で開発するときよくコーディング規約を策定しますが、未策定であっても以下の標準的な規約に沿って記載する必要

https://kotlinlang.org/docs/reference/coding-conventions.html

Android Studio の Reformat Code の機能を活用する必要がある。

* MVVMパターンのView にあたる Activity にてロジックが集約されているので、責務に応じて ViewModel や Model にロジックを移する。

例
MainActivity#onCreate にて allTodoItemList を購読し、並べ替えを実施後に todoAdapter に表示リスト設定する部分は、以下のように直接的に adapter への表示リストを提供する。

val displayTodoItemList: LiveData<List<TodoItem>> = Transformations.map(todoItems) { allList ->

    val itemsWithNoDeadline = mutableListOf<TodoItem>()
    val completedItems = mutableListOf<TodoItem>()
    val otherItems = mutableListOf<TodoItem>()

    allList.forEach { item ->
        when {
            item.dueTime!!.toInt() == 0 && !item.completed -> itemsWithNoDeadline.add(item)
            item.completed -> completedItems.add(item)
            else -> otherItems.add(item)
        }
    }
    otherItems.sortBy { it.dueTime }

    otherItems + itemsWithNoDeadline + completedItems }

※ フィルター機能もViewModel にて実装を検討した方が良いかもしれません。


Next
----
* Activity / Fragment はシンプルに保つ。
* !! をできるだけ使わないようにリファクタリングする。
* テストコードを記述する。
* UI/UXにこだわりを持つ。
* ViewModel / LiveData を活用する
* Coroutine を使うときは CoroutineScope を意識する
