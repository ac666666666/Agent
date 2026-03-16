<template>
  <div class="human-feedback-area my-4 pa-5 rounded-lg">
    <div
      class="d-flex align-center gap-2 mb-4 text-primary text-subtitle-1 font-weight-medium"
    >
      <v-icon color="primary">mdi-comment-processing-outline</v-icon>
      <span>请对智能体的计划进行评价</span>
    </div>

    <div class="feedback-input mb-4">
      <v-textarea
        v-model="feedbackInput"
        rows="3"
        placeholder="请输入您的反馈意见（可选）..."
        counter="500"
        variant="outlined"
        bg-color="white"
      ></v-textarea>
    </div>

    <div class="d-flex justify-end gap-3 flex-column flex-sm-row">
      <v-btn
        color="success"
        prepend-icon="mdi-check"
        @click="submitFeedback(false)"
      >
        通过计划
      </v-btn>
      <v-btn
        color="error"
        prepend-icon="mdi-close"
        @click="submitFeedback(true)"
      >
        不通过计划
      </v-btn>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import type { GraphRequest } from "~/services/graph";

const props = defineProps<{
  request: GraphRequest;
  handleFeedback: (
    request: GraphRequest,
    rejectedPlan: boolean,
    content: string
  ) => Promise<void>;
}>();

const feedbackInput = ref("");

const submitFeedback = (rejectedPlan: boolean) => {
  const feedbackContent = feedbackInput.value.trim() || "Accept";
  props.handleFeedback(props.request, rejectedPlan, feedbackContent);
  feedbackInput.value = "";
};
</script>

<style scoped>
.human-feedback-area {
  background: #f8fbff;
  border: 1px solid #e1f0ff;
}
</style>