<template>
  <div class="preset-questions-wrapper">
    <v-card variant="outlined" class="preset-questions-container">
      <div class="d-flex align-center gap-2 mb-3 pb-2 border-b">
        <v-icon color="primary" size="small">mdi-chat-question-outline</v-icon>
        <span class="text-subtitle-2 font-weight-medium text-grey-darken-2"
          >预设问题</span
        >
      </div>

      <div
        v-if="loading"
        class="d-flex align-center justify-center py-3 text-caption text-grey"
      >
        <v-progress-circular
          indeterminate
          size="16"
          width="2"
          color="primary"
          class="mr-2"
        ></v-progress-circular>
        加载中...
      </div>

      <div
        v-else-if="activeQuestions.length === 0"
        class="d-flex align-center justify-center py-3 text-caption text-grey"
      >
        暂无预设问题
      </div>

      <div v-else class="d-flex flex-wrap gap-2">
        <v-chip
          v-for="question in activeQuestions"
          :key="question.id"
          class="question-item cursor-pointer"
          color="primary"
          variant="tonal"
          label
          @click="handleQuestionClick(question)"
        >
          <span class="text-truncate" style="max-width: 200px">{{
            question.question
          }}</span>
          <template v-slot:append>
            <v-icon size="small" end>mdi-chevron-right</v-icon>
          </template>
        </v-chip>
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import PresetQuestionService from "~/services/presetQuestion";
import type { PresetQuestion } from "~/services/presetQuestion";

const props = defineProps<{
  agentId: number;
  onQuestionClick: (question: string) => void;
}>();

const questions = ref<PresetQuestion[]>([]);
const loading = ref(false);

const activeQuestions = computed(() => {
  return questions.value.filter((q) => q.isActive !== false);
});

const loadPresetQuestions = async () => {
  loading.value = true;
  try {
    questions.value = await PresetQuestionService.list(props.agentId);
  } catch (error) {
    console.error("加载预设问题失败", error);
  } finally {
    loading.value = false;
  }
};

const handleQuestionClick = (question: PresetQuestion) => {
  if (props.onQuestionClick) {
    props.onQuestionClick(question.question);
  }
};

onMounted(() => {
  loadPresetQuestions();
});
</script>

<style scoped>
.preset-questions-wrapper {
  margin-bottom: 16px;
}

.preset-questions-container {
  padding: 12px 16px;
  background: white;
  border-color: #e8e8e8;
}

.border-b {
  border-bottom: 1px solid #f0f0f0;
}

.question-item {
  transition: all 0.2s ease;
}

.question-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
</style>