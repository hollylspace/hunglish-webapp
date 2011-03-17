grep "\(^<s>\)\|\(^  <p>\)" | sed "s/^<s>//" | sed "s/^  <p>/<p>/"
