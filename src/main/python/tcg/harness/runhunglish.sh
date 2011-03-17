ROOT=/home/bpgergo/hunglish_data/
rm -rf $ROOT/hu/sen/*
rm -rf $ROOT/hu/tok/*
rm -rf $ROOT/hu/stemcache/*
rm -rf $ROOT/hu/stem/*
rm -rf $ROOT/en/sen/*
rm -rf $ROOT/en/tok/*
rm -rf $ROOT/en/stemcache/*
rm -rf $ROOT/en/stem/*
rm -rf $ROOT/align/*
python harness.py --graph=hunglishstategraph.txt --commands=hunglishcommands.txt --root=$ROOT
